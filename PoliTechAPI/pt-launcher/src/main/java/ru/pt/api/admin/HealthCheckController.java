package ru.pt.api.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CoefficientDataRow;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.product.ProductService;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.TextDocumentView;
import ru.pt.domain.model.VariableContext;
import ru.pt.domain.model.VariableContextImpl;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admins/checks")
public class HealthCheckController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
    
    private final StorageService storageService;
    private final ProductService productService;
    private final CalculatorService calculatorService;
    private final CoefficientService coefficientService;
    private final TextDocumentView textDocumentView;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/policy/{policyNumber}")
    public Map<String, Object> checkPolicy(
            @PathVariable String policyNumber,
            @RequestBody(required = false) String body) {
        logger.info("Health check for policy: {}", policyNumber);
        // Get policy by number from DB
        PolicyData policyData = storageService.getPolicyByNumber(policyNumber);
        logger.debug("Policy data retrieved for policyNumber: {}", policyNumber);
        
        // Get product code and product version from policy
        String productCode = policyData.getPolicyIndex().getProductCode();
        Integer versionNo = policyData.getPolicyIndex().getVersionNo();
        logger.debug("Product info: productCode={}, versionNo={}", productCode, versionNo);
        
        // Take policyVersionData from DB
        ProductVersionModel productVersion = productService.getProductByCodeAndVersionNo(productCode, versionNo);
        logger.debug("Product version retrieved with {} variables", productVersion.getVars().size());
        
        // Create varContext from policyVersionData
        List<PvVarDefinition> varDefinitions = productVersion.getVars().stream()
                .map(this::toDefinition)
                .toList();
        
        VariableContext varContext = new VariableContextImpl(policyData.getPolicy(), varDefinitions);
        logger.debug("Variable context created with {} definitions", varDefinitions.size());
        
        // Call get for all var to init databind
        Map<String, String> vars = new HashMap<>();
        for (PvVarDefinition varDef : varDefinitions) {
            String varCode = varDef.getCode();
            logger.debug("VARCODE={}", varCode);
            String varValue = varContext.getString(varCode);
            logger.debug("VARVALUE={}", varValue);
            vars.put(varCode, varValue);
        }
        logger.debug("Retrieved {} variable values", vars.size());
        
        // Return map with varName and varValue and policyData
        Map<String, Object> result = new HashMap<>();
        result.put("policyData", policyData);
        result.put("vars", vars);
        
        // Process text if provided in body
        if (body != null) {
            String text = body;
            logger.debug("Text provided in body, processing with TextDocumentView");
            //String testResult = textDocumentView.get(varContext, text);
            //result.put("TEXT TEST RESULT", testResult);
            //logger.debug("Text processing completed, result length: {}", testResult != null ? testResult.length() : 0);
        }
        
        logger.info("Health check completed for policy: {}", policyNumber);
        return result;
    }
    
    @GetMapping("/product/{productId}/version/{versionNo}")
    public ProductVersionDetails getProductVersionDetails(
            @PathVariable Integer productId,
            @PathVariable Integer versionNo) {
        
        logger.info("Getting product version details: productId={}, versionNo={}", productId, versionNo);
        
        // Get product version from DB
        ProductVersionModel productVersion = productService.getVersion(productId, versionNo);
        logger.debug("Product version retrieved: {} packages", productVersion.getPackages().size());
        
        // Collect calculators and coefficients for each package
        List<PackageDetails> packageDetailsList = new ArrayList<>();
        
        for (var pkg : productVersion.getPackages()) {
            logger.debug("Processing package: {}", pkg.getCode());
            
            // Get calculator for this package
            CalculatorModel calculator = calculatorService.getCalculator(productId, versionNo, pkg.getCode());
            logger.debug("Calculator retrieved for package: {}", pkg.getCode());
            
            // Get coefficients for this calculator
            List<CoefficientData> coefficients = new ArrayList<>();
            if (calculator != null && calculator.getCoefficients() != null) {
                for (var coef : calculator.getCoefficients()) {
                    logger.debug("Getting coefficient table: {}", coef.getVarCode());
                    List<CoefficientDataRow> coefficientTable = coefficientService.getTable(
                        calculator.getId(),
                        coef.getVarCode()
                    );
                    if (coefficientTable != null) {
                        coefficients.add(new CoefficientData(
                            coef.getVarCode(),
                            coef.getVarName(),
                            coefficientTable
                        ));
                    }
                }
            }
            logger.debug("Retrieved {} coefficient tables for package: {}", coefficients.size(), pkg.getCode());
            
            packageDetailsList.add(new PackageDetails(
                pkg.getCode(),
                pkg.getName(),
                calculator,
                coefficients
            ));
        }
        
        logger.info("Product version details completed: {} packages processed", packageDetailsList.size());
        
        return new ProductVersionDetails(
            productVersion,
            packageDetailsList
        );
    }
    
    @PostMapping("/product/version/import")
    public ImportResult importProductVersionDetails(@RequestBody ProductVersionDetails details) {
        
        logger.info("Importing product version details for product: {}", 
            details.productVersion().getCode());
        
        int createdCalculators = 0;
        int createdCoefficients = 0;
        List<String> errors = new ArrayList<>();
        boolean versionCreated = false;
        
        try {
            Integer productId = details.productVersion().getId();
            Integer versionNo = details.productVersion().getVersionNo();
            String productCode = details.productVersion().getCode();
            
            logger.debug("Importing for productId={}, productCode={}, versionNo={}", 
                productId, productCode, versionNo);
            
            // Get current version from DB
            ProductVersionModel currentVersion = productService.getVersion(productId, versionNo);
            logger.debug("Current version status: {}", currentVersion.getVersionStatus());
            
            // Handle product version based on status
            if ("PROD".equalsIgnoreCase(currentVersion.getVersionStatus())) {
                // If PROD, create new dev version
                logger.info("Version is PROD, creating new dev version from version {}", versionNo);
                ProductVersionModel newDevVersion = productService.createVersionFrom(productId, versionNo);
                versionCreated = true;
                versionNo = newDevVersion.getVersionNo();
                logger.info("Created new dev version: {}", versionNo);
                
                // Update the new dev version with imported data
                productService.updateVersion(productId, versionNo, details.productVersion());
                logger.debug("Updated new dev version with imported data");
                
            } else if ("DEV".equalsIgnoreCase(currentVersion.getVersionStatus())) {
                // If DEV, update existing version
                logger.info("Version is DEV, updating version {}", versionNo);
                productService.updateVersion(productId, versionNo, details.productVersion());
                logger.debug("Updated dev version with imported data");
                
            } else {
                String error = "Version status must be PROD or DEV, found: " + currentVersion.getVersionStatus();
                logger.error(error);
                errors.add(error);
                return new ImportResult(false, 0, 0, errors, false, null);
            }
            
            // Process each package
            for (PackageDetails pkgDetails : details.packages()) {
                logger.debug("Processing package: {}", pkgDetails.packageCode());
                
                try {
                    // Create or update calculator for this package
                    if (pkgDetails.calculator() != null) {
                        // Check if calculator exists
                        CalculatorModel existingCalculator = calculatorService.getCalculator(
                            productId, 
                            versionNo, 
                            pkgDetails.packageCode()
                        );
                        
                        if (existingCalculator == null) {
                            // Create new calculator
                            calculatorService.createCalculatorIfMissing(
                                productId,
                                productCode,
                                versionNo, 
                                pkgDetails.packageCode()
                            );
                            createdCalculators++;
                            logger.debug("Created calculator for package: {}", pkgDetails.packageCode());
                            
                            // Get the newly created calculator to get its ID
                            existingCalculator = calculatorService.getCalculator(
                                productId, 
                                versionNo, 
                                pkgDetails.packageCode()
                            );
                        }
                        
                        // Import coefficients for this calculator
                        if (pkgDetails.coefficients() != null && existingCalculator != null) {
                            for (CoefficientData coefData : pkgDetails.coefficients()) {
                                try {
                                    logger.debug("Importing coefficient: {} for calculator: {}", 
                                        coefData.code(), existingCalculator.getId());
                                    
                                    // Replace coefficient table data
                                    coefficientService.replaceTable(
                                        existingCalculator.getId(),
                                        coefData.code(),
                                        coefData.data()
                                    );
                                    createdCoefficients++;
                                    logger.debug("Imported coefficient table: {}", coefData.code());
                                    
                                } catch (Exception e) {
                                    String error = String.format(
                                        "Failed to import coefficient %s for package %s: %s",
                                        coefData.code(), pkgDetails.packageCode(), e.getMessage()
                                    );
                                    logger.error(error, e);
                                    errors.add(error);
                                }
                            }
                        }
                    }
                    
                } catch (Exception e) {
                    String error = String.format(
                        "Failed to process package %s: %s",
                        pkgDetails.packageCode(), e.getMessage()
                    );
                    logger.error(error, e);
                    errors.add(error);
                }
            }
            
            logger.info("Import completed: version created={}, version={}, {} calculators, {} coefficients created, {} errors",
                versionCreated, versionNo, createdCalculators, createdCoefficients, errors.size());
            
            return new ImportResult(
                true,
                createdCalculators,
                createdCoefficients,
                errors.isEmpty() ? null : errors,
                versionCreated,
                versionNo
            );
            
        } catch (Exception e) {
            logger.error("Import failed: {}", e.getMessage(), e);
            errors.add("Import failed: " + e.getMessage());
            return new ImportResult(
                false,
                createdCalculators,
                createdCoefficients,
                errors,
                versionCreated,
                null
            );
        }
    }
    


    @PostMapping("/dumpmetadata")
    public Map<String, Object> dumpMetadata(@RequestBody Map<String, Object> policyJson) {
        logger.info("Dumping metadata for policy JSON");
        
        // 1. Select all from pt_metadata using JdbcTemplate
        String sql = "SELECT var_code, var_name, var_path, var_type, var_value, var_cdm, nr, var_data_type FROM vw_metadata ";

        List<PvVar> pvVars = jdbcTemplate.query(sql, new PvVarRowMapper());
        logger.debug("Retrieved {} variables from pt_metadata", pvVars.size());
        
        // 2. Convert to List<PvVarDefinition>
        List<PvVarDefinition> varDefinitions = pvVars.stream()
                .map(PvVarDefinition::fromPvVar)
                .toList();
        logger.debug("Converted {} variables to PvVarDefinition", varDefinitions.size());
        
        // 3. Convert policy JSON map to JSON string and create VariableContext
        String policyJsonString;
        try {
            policyJsonString = objectMapper.writeValueAsString(policyJson);
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize policy JSON: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to serialize policy JSON", e);
        }
        VariableContext varCtx = new VariableContextImpl(policyJsonString, varDefinitions);
        logger.debug("Created VariableContext with {} definitions", varDefinitions.size());
        
        // 4. For all vars, call varCtx.getString(varCode) and put into map1
        Map<String, String> map1 = new HashMap<>();
        for (PvVarDefinition varDef : varDefinitions) {
            String varCode = varDef.getCode();
            String value = varCtx.getString(varCode);
            map1.put(varCode, value);
            logger.trace("Context value for {}: {}", varCode, value);
        }
        logger.debug("Retrieved {} context values", map1.size());
        
        // 5. For all vars, call textDocumentView.get(varCtx, varCode) and put into map2
        Map<String, String> map2 = new HashMap<>();
        for (PvVarDefinition varDef : varDefinitions) {
            String varCode = varDef.getCode();
            String value = textDocumentView.get(varCtx, varCode);
            map2.put(varCode, value);
            logger.trace("Text value for {}: {}", varCode, value);
        }
        logger.debug("Retrieved {} text values", map2.size());
        
        // 6. Return JSON with context and text maps
        Map<String, Object> result = new HashMap<>();
        result.put("context", map1);
        result.put("text", map2);
        
        logger.info("Metadata dump completed. context entries: {}, text entries: {}", map1.size(), map2.size());
        return result;
    }

    private PvVarDefinition toDefinition(ru.pt.api.dto.product.PvVar var) {
        PvVarDefinition.Type type;
        switch (var.getVarDataType()) {
            case NUMBER:
                type = PvVarDefinition.Type.NUMBER;
                break;
            case STRING:
            default:
                type = PvVarDefinition.Type.STRING;
                break;
        }
        return PvVarDefinition.fromPvVar(var); 
    }
    
    /**
     * RowMapper for mapping pt_metadata table rows to PvVar objects
     */
    private static class PvVarRowMapper implements RowMapper<PvVar> {
        @Override
        public PvVar mapRow(ResultSet rs, int rowNum) throws SQLException {
            PvVar pvVar = new PvVar();
            pvVar.setVarCode(rs.getString("var_code"));
            pvVar.setVarName(rs.getString("var_name"));
            pvVar.setVarPath(rs.getString("var_path"));
            pvVar.setVarType(rs.getString("var_type"));
            pvVar.setVarValue(rs.getString("var_value"));
            pvVar.setVarCdm(rs.getString("var_cdm"));
            pvVar.setVarNr(rs.getString("nr"));
            
            // Convert var_data_type string to VarDataType enum
            String varDataTypeStr = rs.getString("var_data_type");
            if (varDataTypeStr != null) {
                try {
                    pvVar.setVarDataType(VarDataType.valueOf(varDataTypeStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // Default to STRING if enum value not found
                    pvVar.setVarDataType(VarDataType.STRING);
                }
            } else {
                pvVar.setVarDataType(VarDataType.STRING);
            }
            
            return pvVar;
        }
    }
    
    // Response DTOs
    public record ProductVersionDetails(
        ProductVersionModel productVersion,
        List<PackageDetails> packages
    ) {}
    
    public record PackageDetails(
        Integer packageCode,
        String packageName,
        CalculatorModel calculator,
        List<CoefficientData> coefficients
    ) {}
    
    public record CoefficientData(
        String code,
        String name,
        List<CoefficientDataRow> data
    ) {}
    
    public record ImportResult(
        boolean success,
        int calculatorsCreated,
        int coefficientsImported,
        List<String> errors,
        boolean versionCreated,
        Integer targetVersionNo
    ) {}
}

package ru.pt.api.admin;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.product.ProductService;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;

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

    @GetMapping("/policy/{policyNumber}")
    public Map<String, Object> checkPolicy(@PathVariable String policyNumber) {
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
        
        VariableContext varContext = new VariableContext(policyData.getPolicy(), varDefinitions);
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
                    ArrayNode coefficientTable = coefficientService.getTable(
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
        return new PvVarDefinition(
            var.getVarCode(),
            var.getVarPath(),
            type,
            var.getVarType()
        );
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
        ArrayNode data
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

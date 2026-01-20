package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.product.ProductService;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;

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
}

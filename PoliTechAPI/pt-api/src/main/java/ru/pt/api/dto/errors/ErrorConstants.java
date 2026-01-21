package ru.pt.api.dto.errors;

import java.util.List;

/**
 * Constants for error domains, reasons, and helper methods for creating ErrorModel
 */
public class ErrorConstants {
    
    // Error Domains
    public static final String DOMAIN_POLICY = "policy";
    public static final String DOMAIN_PRODUCT = "product";
    public static final String DOMAIN_AUTH = "auth";
    public static final String DOMAIN_VALIDATION = "validation";
    public static final String DOMAIN_STORAGE = "storage";
    public static final String DOMAIN_FILE = "file";
    public static final String DOMAIN_CALCULATOR = "calculator";
    public static final String DOMAIN_PAYMENT = "payment";
    
    // Error Reasons
    public static final String REASON_NOT_FOUND = "notFound";
    public static final String REASON_INVALID = "invalid";
    public static final String REASON_UNAUTHORIZED = "unauthorized";
    public static final String REASON_FORBIDDEN = "forbidden";
    public static final String REASON_VALIDATION_FAILED = "validationFailed";
    public static final String REASON_MISSING_REQUIRED = "missingRequired";
    public static final String REASON_INVALID_FORMAT = "invalidFormat";
    public static final String REASON_INTERNAL_ERROR = "internalError";
    public static final String REASON_SERVICE_UNAVAILABLE = "serviceUnavailable";
    
    // Helper methods for creating ErrorModel with ErrorDetail
    public static ErrorModel createErrorModel(int code, String message, String domain, String reason, String field) {
        ErrorModel.ErrorDetail detail = new ErrorModel.ErrorDetail(domain, reason, message, field);
        return new ErrorModel(code, message, List.of(detail));
    }
    
    public static ErrorModel createErrorModel(int code, String message, String domain, String reason) {
        return createErrorModel(code, message, domain, reason, null);
    }
    
    // Common error messages
    public static String policyNotFound(String policyNumber) {
        return String.format("Policy not found: %s", policyNumber);
    }
    
    public static String policyNotFoundById(String policyId) {
        return String.format("Policy not found by ID: %s", policyId);
    }
    
    public static String productNotFound(String productCode) {
        return String.format("Product not found: %s", productCode);
    }
    
    public static String productVersionNotFound(String productCode, Integer versionNo) {
        return String.format("Product version not found: productCode=%s, versionNo=%d", productCode, versionNo);
    }
    
    public static String invalidJsonFormat(String location) {
        return String.format("Invalid JSON format at: %s", location);
    }
    
    public static String unauthorizedAccess(String resource) {
        return String.format("Unauthorized access to resource: %s", resource);
    }
    
    public static String forbiddenAccess(String resource, String reason) {
        return String.format("Access forbidden to resource: %s. Reason: %s", resource, reason);
    }
    
    public static String validationFailed(String field, String reason) {
        return String.format("Validation failed for field '%s': %s", field, reason);
    }
    
    public static String missingRequiredField(String field) {
        return String.format("Required field is missing: %s", field);
    }
    
    public static String fileNotFound(String fileId) {
        return String.format("File not found: %s", fileId);
    }
    
    public static String printFormNotFound(String policyNumber, String printFormType) {
        return String.format("Print form not found: policyNumber=%s, printFormType=%s", policyNumber, printFormType);
    }
}

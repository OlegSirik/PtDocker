package ru.pt.api.dto.rules;

public enum RuleType {
    PRE_QUOTE_VALIDATION,
    POST_QUOTE_VALIDATION,
    PRE_SAVE_VALIDATION,
    POST_SAVE_VALIDATION,
    QUOTE_CALCULATION,
    UNDERWRITING,
    WORKFLOW,
    CROSS_SELL,
    FRAUD_CHECK,
    ISSUANCE,
    RENEWAL;

    public boolean isValidation() {
        return switch (this) {
            case PRE_QUOTE_VALIDATION, POST_QUOTE_VALIDATION, PRE_SAVE_VALIDATION, POST_SAVE_VALIDATION -> true;
            default -> false;
        };
    }
}

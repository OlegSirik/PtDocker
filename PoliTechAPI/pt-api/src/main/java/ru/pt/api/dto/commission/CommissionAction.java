package ru.pt.api.dto.commission;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public enum CommissionAction {
    SALE("sale"),
    PROLONGATION("prolongation");
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CommissionAction.class);
    private static final Map<String, CommissionAction> BY_VALUE = new HashMap<>();

    private final String code;
    
    static {
        for (CommissionAction action : values()) {
            BY_VALUE.put(action.code.toLowerCase(), action);
            BY_VALUE.put(action.name().toLowerCase(), action);
        }
    }

    CommissionAction(String code) {
        this.code = code;
    }
    
    public String getCode() {
        return code;
    }
    
    // Для Jackson аннотаций
    @JsonValue
    public String toValue() {
        return code;
    }
    
    @JsonCreator
    public static CommissionAction fromValue(String value) {
        if (value == null) {
            LOGGER.trace("CommissionAction value is null");
            return null;
        }

        String normalized = value.trim().toLowerCase();
        if (normalized.isEmpty()) {
            LOGGER.trace("CommissionAction value is blank");
            return null;
        }

        CommissionAction action = BY_VALUE.get(normalized);
        if (action == null) {
            LOGGER.warn("Unknown CommissionAction value: {}", value);
        }
        return action;
    }
}

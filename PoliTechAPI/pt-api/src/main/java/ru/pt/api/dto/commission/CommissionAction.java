package ru.pt.api.dto.commission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum CommissionAction {
    SALE("sale"),
    PROLO("prolongation");

    private static final Logger LOGGER = LoggerFactory.getLogger(CommissionAction.class);
    private final String value;

    CommissionAction(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }    

    public static CommissionAction fromString(String value) {
        if (value == null) {
            LOGGER.trace("No value found for CommissionAction: value is null");
            return null;
        }
        try {
            return valueOf(value);
        } catch (Exception e) {
            LOGGER.trace("No value found for CommissionAction: {}", value, e);
            return null;
        }
    }

    public static String toString(CommissionAction value) {
        if (value == null) {
            return null;
        }
        return value.getValue();
    }

}

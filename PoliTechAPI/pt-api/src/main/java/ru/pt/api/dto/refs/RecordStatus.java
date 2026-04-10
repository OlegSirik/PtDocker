package ru.pt.api.dto.refs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecordStatus {
    ACTIVE("ACTIVE"),
    SUSPENDED("SUSPENDED"),
    DELETED("DELETED");

    private final String value;

    RecordStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonValue
    public String toValue() {
        return value;
    }

    @JsonCreator
    public static RecordStatus fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        for (RecordStatus status : values()) {
            if (status.value.equalsIgnoreCase(s)) {
                return status;
            }
        }
        return null;
    }
}

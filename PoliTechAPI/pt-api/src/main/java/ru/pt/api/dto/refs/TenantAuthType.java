package ru.pt.api.dto.refs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Tenant {@code auth_type} values; aligned with {@code ru.pt.auth.model.AuthType}. */
public enum TenantAuthType {
    LOCAL_JWT("LOCAL_JWT"),
    JWT("JWT"),
    HEADERS("HEADERS"),
    NONE("NONE"),
    APIKEY("APIKEY"),
    KEYCLOAK("KEYCLOAK");

    private final String value;

    TenantAuthType(String value) {
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
    public static TenantAuthType fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.trim();
        for (TenantAuthType type : values()) {
            if (type.value.equalsIgnoreCase(s)) {
                return type;
            }
        }
        return null;
    }
}

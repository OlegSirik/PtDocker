package ru.pt.api.dto.auth;

public enum ClientAuthType {

    LOCAL_JWT("LOCAL_JWT"),
    HEADERS("HEADERS"),
    NONE("NONE"),
    APIKEY("APIKEY");

    private final String value;

    ClientAuthType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}

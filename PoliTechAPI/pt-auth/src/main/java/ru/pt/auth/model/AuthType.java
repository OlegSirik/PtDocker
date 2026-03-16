package ru.pt.auth.model;

public enum AuthType {
    LOCAL_JWT("LOCAL_JWT"),
    JWT("JWT"),
    HEADERS("HEADERS"),
    APIKEY("APIKEY"),
    KEYCLOAK("KEYCLOAK"),
    NONE("NONE");

    private final String value;

    AuthType(String value) {
        this.value = value;
    }
    
}

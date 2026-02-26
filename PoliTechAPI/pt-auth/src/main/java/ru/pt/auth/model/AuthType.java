package ru.pt.auth.model;

public enum AuthType {
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

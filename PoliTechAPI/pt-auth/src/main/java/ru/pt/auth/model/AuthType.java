package ru.pt.auth.model;

public enum AuthType {
    JWT("JWT"),
    HEADERS("HEADERS"),
    API_KEY("API_KEY"),
    KEYCLOAK("KEYCLOAK"),
    NONE("NONE");

    private final String value;

    AuthType(String value) {
        this.value = value;
    }
    
}

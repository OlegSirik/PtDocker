package ru.pt.auth.model;

/**
 * Common auth configuration property keys stored in TenantEntity.authConfig.
 * Enum name is UPPER_SNAKE_CASE; value is camelCase key used in JSON.
 */
public enum AuthProperties {
    ISSUER("issuer"),
    JWKS_URI("jwksUri"),
    AUDIENCE("audience"),
    ADMIN_URL("adminUrl"),
    ADMIN_REALM("adminRealm"),
    /** client_id для token endpoint при password grant (например admin-cli) */
    ADMIN_TOKEN_CLIENT_ID("adminTokenClientId"),
    /** логин Keycloak admin user для password grant */
    ADMIN_USERNAME("adminUsername"),
    /** пароль Keycloak admin user для password grant */
    ADMIN_PASSWORD("adminPassword"),
    ADMIN_CLIENT_ID("adminClientId"),
    ADMIN_CLIENT_SECRET("adminClientSecret");

    private final String value;

    AuthProperties(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}


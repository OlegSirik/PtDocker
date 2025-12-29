package ru.pt.auth.model;

public record TenantSecurityConfig(
    String tenantCode,
    AuthType authType,
    // JWT
    String jwtIssuer,
    String jwtPublicKey,
    // Headers
    String clientIdHeader,
    String userIdHeader
    // future: oauth, mtls, etc.
) { }

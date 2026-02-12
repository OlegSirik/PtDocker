package ru.pt.auth.model;

import ru.pt.api.dto.auth.ClientAuthType;

public record ClientSecurityConfig (
    Long id,
    String authClientId,
    Long defaultAccountId,
    Long tid,
    String name,
    AuthType authType,
    ClientAuthType authLevel
) { }

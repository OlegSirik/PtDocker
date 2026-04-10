package ru.pt.auth.model;

import ru.pt.api.dto.auth.ClientAuthLevel;

public record ClientSecurityConfig (
    Long id,
    String authClientId,
    Long defaultAccountId,
    Long tid,
    String name,
    AuthType authType,
    ClientAuthLevel authLevel
) { }

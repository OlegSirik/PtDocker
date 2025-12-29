package ru.pt.auth.model;

public record ClientSecurityConfig (
    Long id,
    String authClientId,
    Long defaultAccountId,
    Long tid,
    String name
) { }

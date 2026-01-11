package ru.pt.auth.model;

public record AdminResponse(
    Long id,
    Long tid,
    String tenantCode,
    Long clientId,
    Long accountId,
    String userLogin,
    String userRole,
    String fullName,
    String position
) { }

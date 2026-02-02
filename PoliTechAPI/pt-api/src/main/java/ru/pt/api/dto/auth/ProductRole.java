package ru.pt.api.dto.auth;

public record ProductRole(
    Long id,
    Long tid,
    Long clientId,
    Long accountId,
    Long roleProductId,
    String roleProductName,
    Long roleAccountId,
    Boolean isDeleted,
    Boolean canRead,
    Boolean canQuote,
    Boolean canPolicy,
    Boolean canAddendum,
    Boolean canCancel,
    Boolean canProlongate
) {}
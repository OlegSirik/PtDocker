package ru.pt.api.dto.auth;

public record Account(
    Long id,
    Long tid,
    Long clientId,
    Long parentId,
    String nodeType,
    String name
) {}

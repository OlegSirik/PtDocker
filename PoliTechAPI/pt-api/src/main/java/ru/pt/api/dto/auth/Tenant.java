package ru.pt.api.dto.auth;

import java.time.LocalDateTime;

public record Tenant(
    Long id,
    String name,
    Boolean isDeleted,
    String authType,
    String code,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) { }
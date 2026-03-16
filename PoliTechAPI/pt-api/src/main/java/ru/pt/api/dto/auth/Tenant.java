package ru.pt.api.dto.auth;

import java.time.LocalDateTime;
import java.util.Map;

public record Tenant(
    Long id,
    String name,
    Boolean isDeleted,
    String authType,
    String storageType,
    String code,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<String, String> storageConfig,
    Map<String, String> authConfig
) { }
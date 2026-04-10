package ru.pt.api.dto.auth;

import java.time.LocalDateTime;
import java.util.Map;

import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.dto.refs.TenantAuthType;

public record Tenant(
    Long id,
    String name,
    RecordStatus recordStatus,
    TenantAuthType authType,
    FileStorageType storageType,
    String code,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Map<String, String> storageConfig,
    Map<String, String> authConfig
) { }
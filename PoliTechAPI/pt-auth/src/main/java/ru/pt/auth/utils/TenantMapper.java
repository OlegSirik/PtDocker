package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Tenant;

import java.util.HashMap;
import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.api.dto.refs.TenantAuthType;
import ru.pt.api.dto.file.FileStorageType;
import ru.pt.auth.entity.TenantEntity;

@Component
public class TenantMapper {

    public TenantEntity toEntity(Tenant dto) {
        if (dto == null) {
            return null;
        }

        TenantEntity entity = new TenantEntity();
        entity.setId(dto.id());
        entity.setName(dto.name());
        entity.setRecordStatus(dto.recordStatus() != null ? dto.recordStatus().getValue() : null);
        entity.setCreatedAt(dto.createdAt());
        entity.setUpdatedAt(dto.updatedAt());
        entity.setAuthType(dto.authType().getValue());
        entity.setStorageType(dto.storageType().getValue());
        entity.setCode(dto.code());
        entity.setStorageConfig(dto.storageConfig());
        entity.setAuthConfig(dto.authConfig());
        return entity;
    }

    public Tenant toDto(TenantEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Tenant(
                entity.getId(),
                entity.getName(),
                entity.getRecordStatus() != null ? RecordStatus.valueOf(entity.getRecordStatus()) : null,
                entity.getAuthType() != null ? TenantAuthType.fromValue(entity.getAuthType()) : null,
                entity.getStorageType() != null ? FileStorageType.valueOf(entity.getStorageType()) : null,
                entity.getCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getStorageConfig(),
                entity.getAuthConfig()
        );
    }

}
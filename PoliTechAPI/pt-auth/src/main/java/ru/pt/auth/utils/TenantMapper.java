package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Tenant;
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
        entity.setDeleted(dto.isDeleted());
        entity.setCreatedAt(dto.createdAt());
        entity.setUpdatedAt(dto.updatedAt());
        entity.setAuthType(dto.authType());
        entity.setCode(dto.code());
        return entity;
    }

    public Tenant toDto(TenantEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Tenant(
                entity.getId(), 
                entity.getName(), 
                entity.getDeleted(), 
                entity.getAuthType(),
                entity.getCode(),
                entity.getCreatedAt(), 
                entity.getUpdatedAt()
            );
    }

}
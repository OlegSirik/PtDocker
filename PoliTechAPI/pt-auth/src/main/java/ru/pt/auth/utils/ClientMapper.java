package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Client;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.service.ClientConfigurationMapper;

@Component
public class ClientMapper {

    private final ClientConfigurationMapper clientConfigurationMapper =
            new ClientConfigurationMapper();

    public ClientEntity toEntity(Client dto) {
        if (dto == null) {
            return null;
        }

        ClientEntity entity = new ClientEntity();
        entity.setId(dto.getId());

        // Создаем Tenant только с id для связи
        if (dto.getTid() != null) {
            TenantEntity tenant = new TenantEntity();
            tenant.setId(dto.getTid());
            entity.setTenant(tenant);
        }

        entity.setClientId(dto.getClientId());
        entity.setDefaultAccountId(dto.getDefaultAccountId());
        entity.setName(dto.getName());
        entity.setDeleted(dto.getIsDeleted());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        if (dto.getClientConfiguration() != null) {
            entity.setClientConfigurationEntity(
                    clientConfigurationMapper.toEntity(dto.getClientConfiguration())
            );
        }

        return entity;
    }

    public Client toDto(ClientEntity entity) {
        if (entity == null) {
            return null;
        }

        Client dto = new Client();
        dto.setId(entity.getId());
        dto.setTid(entity.getTenant() != null ? entity.getTenant().getId() : null);
        dto.setClientId(entity.getClientId());
        dto.setDefaultAccountId(entity.getDefaultAccountId());
        dto.setName(entity.getName());
        dto.setIsDeleted(entity.getDeleted());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getClientConfigurationEntity() != null) {
            dto.setClientConfiguration(
                    clientConfigurationMapper.toDto(entity.getClientConfigurationEntity())
            );
        }

        return dto;
    }

}
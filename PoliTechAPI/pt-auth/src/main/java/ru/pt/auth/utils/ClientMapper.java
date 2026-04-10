package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientAuthLevel;
import ru.pt.api.dto.auth.ClientAuthType;
import ru.pt.api.dto.refs.RecordStatus;
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

        if (dto.getTid() != null) {
            TenantEntity tenant = new TenantEntity();
            tenant.setId(dto.getTid());
            entity.setTenant(tenant);
        }

        entity.setAuthClientId(dto.getAuthClientId());
        entity.setDefaultAccountId(dto.getDefaultAccountId());
        entity.setName(dto.getName());
        entity.setRecordStatus(
                dto.getRecordStatus() != null ? dto.getRecordStatus().getValue() : "ACTIVE");
        entity.setAuthType(
                dto.getAuthType() != null ? dto.getAuthType().getValue() : "LOCAL_JWT");
        entity.setAuthLevel(
                dto.getAuthLevel() != null ? dto.getAuthLevel().getValue() : "CLIENT");
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
        dto.setAuthClientId(entity.getAuthClientId());
        dto.setDefaultAccountId(entity.getDefaultAccountId());
        dto.setName(entity.getName());

        RecordStatus recordStatus = RecordStatus.fromValue(entity.getRecordStatus());
        dto.setRecordStatus(recordStatus != null ? recordStatus : RecordStatus.ACTIVE);

        dto.setAuthType(parseClientAuthType(entity.getAuthType()));
        dto.setAuthLevel(parseClientAuthLevel(entity.getAuthLevel()));
        if (entity.getClientConfigurationEntity() != null) {
            dto.setClientConfiguration(
                    clientConfigurationMapper.toDto(entity.getClientConfigurationEntity())
            );
        }

        return dto;
    }

    private static ClientAuthType parseClientAuthType(String raw) {
        if (raw == null || raw.isBlank()) {
            return ClientAuthType.LOCAL_JWT;
        }
        String s = raw.trim();
        for (ClientAuthType t : ClientAuthType.values()) {
            if (t.getValue().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return ClientAuthType.LOCAL_JWT;
    }

    private static ClientAuthLevel parseClientAuthLevel(String raw) {
        if (raw == null || raw.isBlank()) {
            return ClientAuthLevel.CLIENT;
        }
        String s = raw.trim();
        for (ClientAuthLevel l : ClientAuthLevel.values()) {
            if (l.getValue().equalsIgnoreCase(s)) {
                return l;
            }
        }
        return ClientAuthLevel.CLIENT;
    }

}

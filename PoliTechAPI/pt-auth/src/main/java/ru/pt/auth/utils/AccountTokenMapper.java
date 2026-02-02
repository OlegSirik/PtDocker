package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.AccountToken;
import ru.pt.auth.entity.AccountTokenEntity;

import java.util.List;

@Component
public class AccountTokenMapper {

    public AccountToken toDto(AccountTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        AccountToken dto = new AccountToken();
        dto.setId(entity.getId());
        dto.setTid(entity.getTenant() != null ? entity.getTenant().getId() : null);
        dto.setClientId(entity.getClient() != null ? entity.getClient().getId() : null);
        dto.setAccountId(entity.getAccount() != null ? entity.getAccount().getId() : null);
        dto.setToken(entity.getToken());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public List<AccountToken> toDto(List<AccountTokenEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDto)
                .toList();
    }

    public AccountTokenEntity toEntity(AccountToken dto) {
        if (dto == null) {
            return null;
        }

        AccountTokenEntity entity = new AccountTokenEntity();
        entity.setId(dto.getId());
        entity.setToken(dto.getToken());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }
}


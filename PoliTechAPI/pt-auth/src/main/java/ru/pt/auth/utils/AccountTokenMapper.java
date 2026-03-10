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
        dto.setTid(entity.getTid());
        dto.setClientId(entity.getClientId());
        dto.setAccountId(entity.getAccountId());
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
        entity.setTid(dto.getTid());
        entity.setClientId(dto.getClientId());
        entity.setAccountId(dto.getAccountId());
        entity.setToken(dto.getToken());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }
}


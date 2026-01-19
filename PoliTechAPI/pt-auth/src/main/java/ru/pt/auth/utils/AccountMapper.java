package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Account;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountNodeType;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.TenantEntity;

@Component
public class AccountMapper {

    public AccountEntity toEntity(Account dto) {
        if (dto == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setId(dto.id());

        // Tenant
        if (dto.tid() != null) {
            TenantEntity tenant = new TenantEntity();
            tenant.setId(dto.tid());
            entity.setTenant(tenant);
        }

        // Client
        if (dto.clientId() != null) {
            ClientEntity client = new ClientEntity();
            client.setId(dto.clientId());
            entity.setClient(client);
        }

        // Parent
        if (dto.parentId() != null) {
            AccountEntity parent = new AccountEntity();
            parent.setId(dto.parentId());
            entity.setParent(parent);
        }

        entity.setNodeType(AccountNodeType.fromString(dto.nodeType()));
        entity.setName(dto.name());

        return entity;
    }

    public Account toDto(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Account(
            entity.getId(),
            entity.getTenant() != null ? entity.getTenant().getId() : null,
            entity.getClient() != null ? entity.getClient().getId() : null,
            entity.getParent() != null ? entity.getParent().getId() : null,
            entity.getNodeType() != null ? entity.getNodeType().getValue() : null,
            entity.getName()
        );
    }

}
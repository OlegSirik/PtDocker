package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.ProductRoleEntity;
import ru.pt.auth.entity.TenantEntity;

@Component
public class ProductRoleMapper {

    public ProductRoleEntity toEntity(ProductRole dto) {
        if (dto == null) {
            return null;
        }

        ProductRoleEntity entity = new ProductRoleEntity();
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

        // Account
        if (dto.accountId() != null) {
            AccountEntity account = new AccountEntity();
            account.setId(dto.accountId());
            entity.setAccount(account);
        }

        // Role Account
        if (dto.roleAccountId() != null) {
            AccountEntity roleAccount = new AccountEntity();
            roleAccount.setId(dto.roleAccountId());
            entity.setRoleAccount(roleAccount);
        }

        entity.setRoleProductId(dto.roleProductId());
        entity.setDeleted(dto.isDeleted());
        entity.setCanRead(dto.canRead());
        entity.setCanQuote(dto.canQuote());
        entity.setCanPolicy(dto.canPolicy());
        entity.setCanAddendum(dto.canAddendum());
        entity.setCanCancel(dto.canCancel());
        entity.setCanProlongate(dto.canProlongate());

        return entity;
    }

    public ProductRole toDto(ProductRoleEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProductRole(
            entity.getId(),
            entity.getTenant() != null ? entity.getTenant().getId() : null,
            entity.getClient() != null ? entity.getClient().getId() : null,
            entity.getAccount() != null ? entity.getAccount().getId() : null,
            entity.getRoleProductId(),
            entity.getRoleAccount() != null ? entity.getRoleAccount().getId() : null,
            null,
            null,
            null,
            entity.getDeleted(),
            entity.getCanRead(),
            entity.getCanQuote(),
            entity.getCanPolicy(),
            entity.getCanAddendum(),
            entity.getCanCancel(),
            entity.getCanProlongate()
        );
    }

}
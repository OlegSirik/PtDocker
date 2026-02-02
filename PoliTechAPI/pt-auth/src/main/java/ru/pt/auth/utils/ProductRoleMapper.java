package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.ProductRoleEntity;
import ru.pt.auth.entity.TenantEntity;

@Component
public class ProductRoleMapper {
    
    //private final ProductService productService;

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

        entity.setDeleted(Boolean.TRUE.equals(dto.isDeleted()));
        entity.setCanRead(Boolean.TRUE.equals(dto.canRead()));
        entity.setCanQuote(Boolean.TRUE.equals(dto.canQuote()));
        entity.setCanPolicy(Boolean.TRUE.equals(dto.canPolicy()));
        entity.setCanAddendum(Boolean.TRUE.equals(dto.canAddendum()));
        entity.setCanCancel(Boolean.TRUE.equals(dto.canCancel()));
        entity.setCanProlongate(Boolean.TRUE.equals(dto.canProlongate()));

        return entity;
    }

    public ProductRole toDto(ProductRoleEntity entity) {
        return toDto(entity, null);
    }

    public ProductRole toDto(ProductRoleEntity entity, String productName) {
        if (entity == null) {
            return null;
        }

        return new ProductRole(
            entity.getId(),
            entity.getTenant() != null ? entity.getTenant().getId() : null,
            entity.getClient() != null ? entity.getClient().getId() : null,
            entity.getAccount() != null ? entity.getAccount().getId() : null,
            entity.getRoleProductId(),
            productName,
            entity.getRoleAccount() != null ? entity.getRoleAccount().getId() : null,
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
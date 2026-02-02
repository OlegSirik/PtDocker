package ru.pt.auth.service;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.service.auth.AccountHierarchyProvider;
import ru.pt.api.service.auth.AccountProductRoles;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountNodeType;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.ProductRoleRepository;
import ru.pt.auth.utils.AccountMapper;

import java.util.*;

import org.springframework.stereotype.Component;

/**
 * Data access service for account hierarchy and product roles.
 * No authorization checks - pure data access layer.
 * Used by AuthorizationServiceImpl for permission checks.
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountDataService implements AccountProductRoles, AccountHierarchyProvider {

    private final AccountRepository accountRepository;
    private final ProductRoleRepository productRoleRepository;
    private final AccountMapper accountMapper;

    // NodeType constants
    private static final String NODE_GROUP = AccountNodeType.GROUP.name();
    private static final String NODE_ACCOUNT = AccountNodeType.ACCOUNT.name();
    private static final String NODE_SUBACCOUNT = AccountNodeType.SUB.name();

    @Override
    public ProductRole getProductRole(Long accountId, Long productId) {
        List<Map<String, Object>> productRoles = productRoleRepository.findProductRoleForAccountId(accountId, productId);
        if (productRoles.isEmpty()) {
            return null;
        }
        // Find first role with actual id (not just hierarchy node)
        for (Map<String, Object> role : productRoles) {
            if (role.get("id") != null) {
                return mapToProductRole(role, accountId);
            }
        }
        return null;
    }

    @Override
    public List<ProductRole> getProductRolesByAccountIdRaw(Long accountId) {
        List<Map<String, Object>> roles = productRoleRepository.findAllProductRolesByAccountId(accountId);
        Set<String> processedProducts = new HashSet<>();

        return roles.stream()
                .filter(role -> processedProducts.add(String.valueOf(role.get("roleProductCode"))))
                .map(role -> mapToProductRole(role, accountId))
                .toList();
    }

    @Override
    public List<Account> getPathToRoot(Long accountId) {
        List<AccountEntity> list = accountRepository.findPathByAccountId(accountId);
        // convert List<AccountEntity> to List<Account>
        return list.stream()
                .map(accountMapper::toDto)
                .toList();
    }

    @Override
    public boolean isParent(Long parent, Long child) {
        return accountRepository.iCanSeeResource(parent, child);
    }

    /**
     * Get child accounts by parent ID (internal use)
     */
    public List<Account> getChildAccounts(Long parentId) {
        List<AccountEntity> accounts = accountRepository.findByParentId(parentId);
        return accounts.stream()
                .map(accountMapper::toDto)
                .toList();
    }

    /**
     * Get child accounts filtered by node type
     */
    public List<Account> getChildAccountsByType(Long parentId, String nodeType) {
        return getChildAccounts(parentId).stream()
                .filter(account -> nodeType.equals(account.nodeType()))
                .toList();
    }

    /**
     * Get groups under parent account
     */
    public List<Account> getGroups(Long parentId) {
        return getChildAccountsByType(parentId, NODE_GROUP);
    }

    /**
     * Get accounts under parent account
     */
    public List<Account> getAccounts(Long parentId) {
        return getChildAccountsByType(parentId, NODE_ACCOUNT);
    }

    /**
     * Get subaccounts under parent account
     */
    public List<Account> getSubaccounts(Long parentId) {
        return getChildAccountsByType(parentId, NODE_SUBACCOUNT);
    }

    /**
     * Map database result to ProductRole DTO
     */
    public ProductRole mapToProductRole(Map<String, Object> role, Long accountId) {
        return new ProductRole(
                (Long) role.get("id"),
                (Long) role.get("tid"),
                (Long) role.get("client_id"),
                accountId,
                (Long) role.get("role_product_id"),
                role.get("productName") != null ? String.valueOf(role.get("productName")) : null,
                (Long) role.get("role_account_id"),
                Boolean.TRUE.equals(role.get("is_deleted")),
                Boolean.TRUE.equals(role.get("can_read")),
                Boolean.TRUE.equals(role.get("can_quote")),
                Boolean.TRUE.equals(role.get("can_policy")),
                Boolean.TRUE.equals(role.get("can_addendum")),
                Boolean.TRUE.equals(role.get("can_cancel")),
                Boolean.TRUE.equals(role.get("can_prolongate"))
        );
    }
}

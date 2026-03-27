package ru.pt.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountNodeType;
import ru.pt.auth.entity.ProductRoleEntity;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.ProductRoleRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.admin.AdminPermissionHelper;
import ru.pt.auth.utils.AccountMapper;
import ru.pt.auth.utils.ProductRoleMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic service for account management.
 * Contains authorization checks before operations.
 * Uses AccountDataService for pure data access.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ProductRoleRepository productRoleRepository;
    private final AccountMapper accountMapper;
    private final ProductRoleMapper productRoleMapper;
    private final AuthorizationService authService;
    private final SecurityContextHelper securityContextHelper;
    private final AccountDataService accountDataService;
    private final AdminPermissionHelper adminPermissionHelper;
    

    @Override
    public Account getAccountById(Long id) {

        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            id.toString(),   // resourceId 
            id,  // resourceAccountid
            AuthZ.Action.VIEW);

        return accountRepository.findById(id)
                .map(accountMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    @Override
    @Transactional
    public Account createGroup(String name, Long parentId) {
        return createAccount(AccountNodeType.GROUP, name, parentId);
    }

    @Override
    public List<Account> getGroups(Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.VIEW);

        return accountDataService.getGroups(parentId);
    } 

    @Override
    @Transactional
    public Account createAccount(String name, Long parentId) {
        return createAccount(AccountNodeType.ACCOUNT, name, parentId);
    }

    @Override
    public List<Account> getAccounts(Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.LIST);

        return accountDataService.getAccounts(parentId);
    }

    @Override
    public List<Account> getChildren(Long parentId, String nodeType) {
        authService.check(
            getCurrentUser(),
            AuthZ.ResourceType.ACCOUNT,
            parentId.toString(),
            parentId,
            AuthZ.Action.LIST);

        if (nodeType == null || nodeType.isBlank()) {
            return accountDataService.getChildAccounts(parentId);
        }
        return accountDataService.getChildAccountsByType(parentId, nodeType);
    }

    @Override
    @Transactional
    public Account createChild(String name, String nodeType, Long parentId) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Name is required");
        }
        if (nodeType == null || nodeType.isBlank()) {
            throw new BadRequestException("Node type is required");
        }
        AccountNodeType accountNodeType = AccountNodeType.fromString(nodeType.toUpperCase());
        if (accountNodeType == null) {
            throw new BadRequestException("Invalid node type: " + nodeType);
        }
        return createAccount(accountNodeType, name, parentId);
    }

    @Override
    @Transactional
    public Account createSubaccount(String name, Long parentId) {
        return createAccount(AccountNodeType.SUB, name, parentId);
    }

    @Override
    public List<Account> getSubaccounts(Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.VIEW);

        return accountDataService.getSubaccounts(parentId);
    } 

    /********************************** */
    @Override
    public ProductRole getProductRole(Long accountId, Long productId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT_PRODUCT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.VIEW);

        return accountDataService.getProductRole(accountId, productId);
    }

    @Override
    @Transactional
    public ProductRole grantProduct(Long accountId, ProductRole productRole) {

        // проверить доступ к account
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT_PRODUCT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.MANAGE);

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        // Find existing product role for this account and roleProductId
        ProductRoleEntity existingRole = productRoleRepository.findByAccountIdAndProductId(accountId, productRole.roleProductId());

        // Если это унаследованаая роль, то accountId != productRole.accountId
        
        if (existingRole != null) {
            // Update only can_* columns
            existingRole.setCanRead(productRole.canRead());
            existingRole.setCanQuote(productRole.canQuote());
            existingRole.setCanPolicy(productRole.canPolicy());
            existingRole.setCanAddendum(productRole.canAddendum());
            existingRole.setCanCancel(productRole.canCancel());
            existingRole.setCanProlongate(productRole.canProlongate());
            productRoleRepository.save(existingRole);
        } else {
            // Create new entity with accountId for both account and roleAccount
            ProductRoleEntity newRole = productRoleMapper.toEntity(productRole);
            newRole.setTenant(account.getTenant());
            newRole.setClient(account.getClient());
            newRole.setAccount(account);
            newRole.setRoleAccount(account);
            newRole.setIdPath(account.getIdPath());
            newRole.setId(null);
            productRoleRepository.save(newRole);
        }

        // Return complete data with product name from joined query
        return accountDataService.getProductRole(accountId, productRole.roleProductId());
    }

    @Override
    @Transactional
    public void revokeProduct(Long accountId, Long productId) {

        // проверить доступ к account
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT_PRODUCT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.MANAGE);

        // Find and delete the product role directly
        ProductRoleEntity roleToRemove = productRoleRepository.findByAccountIdAndProductId(accountId, productId);

        if (roleToRemove != null) {
            productRoleRepository.delete(roleToRemove);
        }
    }

    @Override
    @Transactional
    public ProductRole assignProductRole(Long accountId, Long roleProductId, Long roleAccountId,
                                         Boolean canRead, Boolean canQuote, Boolean canPolicy,
                                         Boolean canAddendum, Boolean canCancel, Boolean canProlong) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.TNT_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can assign product roles");
        }

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        AccountEntity roleAccount = accountRepository.findById(roleAccountId)
                .orElseThrow(() -> new NotFoundException("Role account not found"));

        if (!account.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot assign role to account in different tenant");
        }

        if (UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            if (!account.getClient().getId().equals(currentUser.getClientId())) {
                throw new ForbiddenException("GROUP_ADMIN can only assign roles within their client");
            }
        }

        ProductRoleEntity role = new ProductRoleEntity();
        role.setTenant(account.getTenant());
        role.setClient(account.getClient());
        role.setAccount(account);
        role.setRoleProductId(roleProductId);
        role.setRoleAccount(roleAccount);
        role.setIdPath(account.getIdPath());
        role.setCanRead(canRead != null && canRead);
        role.setCanQuote(canQuote != null && canQuote);
        role.setCanPolicy(canPolicy != null && canPolicy);
        role.setCanAddendum(canAddendum != null && canAddendum);
        role.setCanCancel(canCancel != null && canCancel);
        role.setCanProlongate(canProlong != null && canProlong);

        ProductRoleEntity saved = productRoleRepository.save(role);
        return productRoleMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void revokeProductRole(Long productRoleId) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.TNT_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can revoke product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        if (!role.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot revoke role from different tenant");
        }

        if (UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            if (!role.getClient().getId().equals(currentUser.getClientId())) {
                throw new ForbiddenException("GROUP_ADMIN can only revoke roles within their client");
            }
        }

        productRoleRepository.deleteById(productRoleId);
    }

    @Override
    @Transactional
    public ProductRole updateProductRole(Long productRoleId, Boolean canRead, Boolean canQuote,
                                        Boolean canPolicy, Boolean canAddendum, Boolean canCancel,
                                        Boolean canProlong) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.TNT_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can update product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        if (!role.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot update role from different tenant");
        }

        if (UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            if (!role.getClient().getId().equals(currentUser.getClientId())) {
                throw new ForbiddenException("GROUP_ADMIN can only update roles within their client");
            }
        }

        if (canRead != null) role.setCanRead(canRead);
        if (canQuote != null) role.setCanQuote(canQuote);
        if (canPolicy != null) role.setCanPolicy(canPolicy);
        if (canAddendum != null) role.setCanAddendum(canAddendum);
        if (canCancel != null) role.setCanCancel(canCancel);
        if (canProlong != null) role.setCanProlongate(canProlong);

        productRoleRepository.save(role);
        return accountDataService.getProductRole(role.getAccount().getId(), role.getRoleProductId());
    }
    
    @Override
    public List<ProductRole> getProductRolesByAccountId(Long accountId) {
        // проверить доступ к account
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT_PRODUCT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.LIST);

        return accountDataService.getProductRolesByAccountIdRaw(accountId);
    }

    @Override
    public List<Account> getAllMyAccounts(String tenantCode, Long clientId, String userLogin) {
        return accountRepository.findByTenantCodeAndClientIdAndUserLogin(tenantCode, clientId, userLogin)
            .stream()
            .map(entity -> {
                Account account = accountMapper.toDto(entity);
                return new Account(
                    account.id(),
                    null,
                    null,
                    null,
                    account.nodeType(),
                    account.name()
                );
            })
            .collect(Collectors.toList());
    }

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    @Override
    public List<Account> getPathToRoot(Long accountId) {
        UserDetailsImpl currentUser = getCurrentUser();
        Long actingAccountId = currentUser.getActingAccountId();
        
        authService.check(
            currentUser, 
            AuthZ.ResourceType.ACCOUNT, 
            accountId.toString(),
            accountId,
            AuthZ.Action.VIEW);

        List<Account> fullList = accountDataService.getPathToRoot(accountId);
        
        List<Account> returnList = new ArrayList<>();
        boolean startCopying = false;
        
        for (Account account : fullList) {

            if (account.id().equals(actingAccountId)) {
                startCopying = true;
            }
            if (startCopying) { returnList.add(account); }

        }
        
        return returnList;
    }

    /********************* */
    public Account createAccount(AccountNodeType accountNodeType, String name, Long parentId) {
        /* Проверить что есть доступ к account под который создается еще один */
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.MANAGE);
        
        /* Находим родительский экаунт */    
        AccountEntity parentAccount = accountRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent account not found: " + parentId));

        /* Добавляем экаунт в БД. Entity формируется через factory. Там же проверяется 
        * допустимость связки paretn-child 
        */        
        try {
            AccountEntity account = AccountEntity.createAccount(parentAccount, name, accountNodeType);
            account.setId(accountRepository.getNextAccountId());
            // Calculate hierarchical id_path from parent
            String parentPath = parentAccount.getIdPath();
            String newPath = (parentPath == null || parentPath.isBlank())
                    ? account.getId().toString()
                    : parentPath + "." + account.getId();
            account.setIdPath(newPath);

            AccountEntity savedAccount = accountRepository.save(account);
            return accountMapper.toDto(savedAccount);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /*
    * Используется только для ролевых экаунтов типа админов. Где всегда один узел определенного типа.
    * для account, sub вернет первую любую.
    */
    @Override
    public Account getRoleAccount(String accountNodeType, Long parentId) {
        AccountNodeType type = AccountNodeType.fromString(accountNodeType);
        if (type == null) {
            throw new BadRequestException("Unknown account node type: " + accountNodeType);
        }
        return getRoleAccount(type, parentId);
    }

    /**
     * Внутренняя перегрузка по enum (pt-auth).
     */
    public Account getRoleAccount(AccountNodeType accountNodeType, Long parentId) {
        authService.check(
            getCurrentUser(), 
            AuthZ.ResourceType.ACCOUNT, 
            parentId.toString(),
            parentId,
            AuthZ.Action.MANAGE);

        List<Account> children = accountDataService.getChildAccounts(parentId); 
        for (Account child : children) {
            if (child.nodeType().equals(accountNodeType.name())) {
                return child;
            }
        }
        return null;
    }

    public Account getOrCreateRoleAccount(AccountNodeType accountNodeType, String name, Long parentId) {
        Account account = getRoleAccount(accountNodeType, parentId);
        if ( account == null ) {
            return createAccount(accountNodeType, name, parentId);
        }
        return account;
    }

 
}

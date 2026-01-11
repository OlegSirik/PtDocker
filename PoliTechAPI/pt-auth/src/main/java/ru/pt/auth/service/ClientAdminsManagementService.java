package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.*;
import ru.pt.auth.repository.*;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.model.AdminResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * Сервис для управления администраторами и пользователями
 * с проверкой прав доступа на основе ролей
 */
@Service
public class ClientAdminsManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ClientAdminsManagementService.class);

    private final TenantService tenantService;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    
    private final ProductRoleRepository productRoleRepository;
    private final SecurityContextHelper securityContextHelper;

    public ClientAdminsManagementService(
            TenantService tenantService,
            ClientRepository clientRepository,
            AccountRepository accountRepository,
            LoginRepository loginRepository,
            AccountLoginRepository accountLoginRepository,
            
            ProductRoleRepository productRoleRepository,
            SecurityContextHelper securityContextHelper) {
        this.tenantService = tenantService;
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        
        this.productRoleRepository = productRoleRepository;
        this.securityContextHelper = securityContextHelper;
    }

   

    // Admin helpers

    private List<AdminResponse> getAdmins(String tenantCode, String userRole) {
        List<Tuple> result = accountLoginRepository.findByTenantAndUserRoleFull(tenantCode, userRole);
        return result.stream()
        .map(tuple -> new AdminResponse(
        tuple.get("id", Long.class),
        tuple.get("tid", Long.class),
        tenantCode, //tuple.get("tenantCode", String.class),
        tuple.get("clientId", Long.class),
        tuple.get("accountId", Long.class),
        tuple.get("userLogin", String.class),
        tuple.get("userRole", String.class),
        tuple.get("fullName", String.class),
        tuple.get("position", String.class)
    ))
    .collect(Collectors.toList());
    }
    

    


    // CLIENT ADMIN MANAGEMENT (TNT_ADMIN only) ==========
    // Создается только tnt admin
    public List<AdminResponse> getClientAdmins(String tenantCode, Long clientId) {

        return getAdmins(tenantCode, UserRole.CLIENT_ADMIN.getValue());
    }

    public AccountLoginEntity createClientAdmin(String tenantCode, String authClientId, String userLogin) {

        AccountEntity clientAccount = accountRepository.findClientAccount(tenantCode, authClientId)
            .orElseThrow(() -> new NotFoundException("Client account not found"));

        AccountLoginEntity accountLogin = AccountLoginEntity.createClientAdmin(clientAccount, userLogin);
        return accountLoginRepository.save(accountLogin);
        
    }

    @Transactional
    public void deleteClientAdmin(Long accountLoginId) {

        AccountLoginEntity accountLogin = accountLoginRepository.findByTenantCodeAndLoginEntityId(getUserImpersonatedTenantCode(), accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        // Проверка прав

        accountLoginRepository.deleteById(accountLoginId);
        logger.info("TNT_ADMIN user deleted: {}", accountLogin.getUserLogin());
    }



    // ========== GROUP_ADMIN MANAGEMENT (TNT_ADMIN only) ==========

    /**
     * TNT_ADMIN: Создание GROUP_ADMIN пользователя
     */
    @Transactional
    public AccountLoginEntity createGroupAdmin(String userLogin, String userName, String fullName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!UserRole.TNT_ADMIN.getValue().equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can create GROUP_ADMIN users");
        }

        String currentTenantCode = currentUser.getTenantCode();
        Long currentClientId = currentUser.getClientId();

        TenantEntity tenant = tenantService.findByCode(currentTenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        ClientEntity client = clientRepository.findById(currentClientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        // Проверка уникальности логина
        if (loginRepository.findByUserLogin(userLogin).isPresent()) {
            throw new BadRequestException("Login '" + userLogin + "' already exists");
        }

        // Создать Login
        LoginEntity login = new LoginEntity();
        login.setTenant(tenant);
        login.setFullName(fullName);
        login.setUserLogin(userLogin);
        LoginEntity savedLogin = loginRepository.save(login);

        // Создать или получить GROUP account
        AccountEntity groupAccount = createGroupAccount(tenant, client, userName);

        // Создать AccountLogin с ролью GROUP_ADMIN
        return createAccountLogin(savedLogin, client, groupAccount);
    }

    @Transactional
    public void deleteGroupAdmin(Long accountLoginId) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!UserRole.TNT_ADMIN.getValue().equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can delete GROUP_ADMIN users");
        }

        AccountLoginEntity accountLogin = accountLoginRepository.findById(accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        // Проверка: GROUP_ADMIN должен быть в том же tenant
        if (!accountLogin.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot delete GROUP_ADMIN from different tenant");
        }

        accountLoginRepository.deleteById(accountLoginId);
        logger.info("GROUP_ADMIN user deleted: {}", accountLogin.getUserLogin());
    }





    // ========== PRODUCT_ROLES MANAGEMENT (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN / GROUP_ADMIN: Выдача роли на продажу продукта
     */
    @Transactional
    public ProductRoleEntity assignProductRole(Long accountId, Long roleProductId, Long roleAccountId,
                                               Boolean canRead, Boolean canQuote, Boolean canPolicy,
                                               Boolean canAddendum, Boolean canCancel, Boolean canProlong) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.TNT_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can assign product roles");
        }

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        AccountEntity roleAccount = accountRepository.findById(roleAccountId)
                .orElseThrow(() -> new NotFoundException("Role account not found"));

        // Проверка: account должен быть в том же tenant
        if (!account.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot assign role to account in different tenant");
        }

        // Для GROUP_ADMIN - дополнительная проверка на client
        if (UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            if (!account.getClient().getId().equals(currentUser.getClientId())) {
                throw new ForbiddenException("GROUP_ADMIN can only assign roles within their client");
            }
        }

        // Создать ProductRole
        ProductRoleEntity role = new ProductRoleEntity();
        role.setTenant(account.getTenant());
        role.setClient(account.getClient());
        role.setAccount(account);
        role.setRoleProductId(roleProductId);
        role.setRoleAccount(roleAccount);
        role.setCanRead(canRead != null && canRead);
        role.setCanQuote(canQuote != null && canQuote);
        role.setCanPolicy(canPolicy != null && canPolicy);
        role.setCanAddendum(canAddendum != null && canAddendum);
        role.setCanCancel(canCancel != null && canCancel);
        role.setCanProlongate(canProlong != null && canProlong);

        return productRoleRepository.save(role);
    }

    /**
     * TNT_ADMIN / GROUP_ADMIN: Отзыв роли на продажу продукта
     */
    @Transactional
    public void revokeProductRole(Long productRoleId) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.TNT_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can revoke product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        // Проверка: role должен быть в том же tenant
        if (!role.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot revoke role from different tenant");
        }

        // Для GROUP_ADMIN - дополнительная проверка на client
        if (UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            if (!role.getClient().getId().equals(currentUser.getClientId())) {
                throw new ForbiddenException("GROUP_ADMIN can only revoke roles within their client");
            }
        }

        productRoleRepository.deleteById(productRoleId);
        logger.info("ProductRole revoked");
    }

    // ========== HELPER METHODS ==========

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }

    private String getUserTenantCode() {
        UserDetailsImpl currentUser = getCurrentUser();
        return currentUser.getTenantCode();
    }

    private String getUserImpersonatedTenantCode() {
        UserDetailsImpl currentUser = getCurrentUser();
        String impersonatedTenantCode = currentUser.getImpersonatedTenantCode();
        if (impersonatedTenantCode == null) {
            return getUserTenantCode();
        }
        return impersonatedTenantCode;
    }

    private AccountEntity getOrCreateTenantAccount(TenantEntity tenant) {
        List<AccountEntity> tenantAccounts = accountRepository.findByNodeType(AccountNodeType.CLIENT);
        return tenantAccounts.stream()
                .filter(a -> a.getTenant().getId().equals(tenant.getId()))
                .findFirst()
                .orElseGet(() -> {
                    AccountEntity account = new AccountEntity();
                    account.setTenant(tenant);
                    account.setNodeType(AccountNodeType.CLIENT);
                    account.setName(tenant.getName());
                    return accountRepository.save(account);
                });
    }


    private AccountEntity createGroupAccount(TenantEntity tenant, ClientEntity client, String groupName) {
        AccountEntity parentAccount = getOrCreateTenantAccount(tenant);

        AccountEntity groupAccount = new AccountEntity();
        groupAccount.setTenant(tenant);
        groupAccount.setClient(client);
        groupAccount.setParent(parentAccount);
        groupAccount.setNodeType(AccountNodeType.GROUP);
        groupAccount.setName(groupName);
        return accountRepository.save(groupAccount);
    }

    private AccountEntity createProductAccount(TenantEntity tenant, ClientEntity client, String productName) {
        AccountEntity groupAccount = accountRepository.findByNodeType(AccountNodeType.GROUP).stream()
                .filter(a -> a.getTenant().getId().equals(tenant.getId()) &&
                        a.getClient().getId().equals(client.getId()))
                .findFirst()
                .orElseGet(() -> createGroupAccount(tenant, client, "Default Group"));

        AccountEntity productAccount = new AccountEntity();
        productAccount.setTenant(tenant);
        productAccount.setClient(client);
        productAccount.setParent(groupAccount);
        productAccount.setNodeType(AccountNodeType.ACCOUNT);
        productAccount.setName(productName);
        return accountRepository.save(productAccount);
    }

    private AccountLoginEntity createAccountLogin(LoginEntity login, ClientEntity client,
                                                   AccountEntity account) {
        AccountLoginEntity accountLogin = new AccountLoginEntity();
        accountLogin.setTenant(login.getTenant());
        accountLogin.setClient(client);
        accountLogin.setAccount(account);
        accountLogin.setLogin(login);
        accountLogin.setUserLogin(login.getUserLogin());
        accountLogin.setDefault(true);
        return accountLoginRepository.save(accountLogin);
    }


    // ========== ACCOUNT MANAGEMENT (GROUP_ADMIN, PRODUCT_ADMIN) ==========

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Создание аккаунта
     */
    @Transactional
    public Map<String, Object> createAccount(Long parentAccountId, String accountName, String nodeType) {
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.SYS_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr) && !UserRole.PRODUCT_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only SYS_ADMIN, GROUP_ADMIN or PRODUCT_ADMIN can create accounts");
        }

        TenantEntity tenant = tenantService.findByCode(currentUser.getTenantCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        ClientEntity client = clientRepository.findById(currentUser.getClientId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        AccountEntity parent = null;
        if (parentAccountId != null) {
            parent = accountRepository.findById(parentAccountId)
                    .orElseThrow(() -> new NotFoundException("Parent account not found"));

            // Проверка: родительский аккаунт должен быть в том же tenant и client
            if (!parent.getTenant().getCode().equals(currentUser.getTenantCode())
                // || !parent.getClient().getId().equals(currentUser.getClientId())
                ) {
                throw new ForbiddenException("Cannot create account under parent from different tenant/client");
            }
        }

        AccountNodeType accountNodeType;
        try {
            accountNodeType = AccountNodeType.fromString(nodeType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid node type: " + nodeType);
        }

        AccountEntity account = new AccountEntity();
        account.setTenant(tenant);
        account.setClient(client);
        account.setParent(parent);
        account.setNodeType(accountNodeType);
        account.setName(accountName);
        AccountEntity saved = accountRepository.save(account);

        Map<String, Object> result = new HashMap<>();
        result.put("id", saved.getId());
        result.put("name", saved.getName());
        result.put("nodeType", saved.getNodeType().toString());
        result.put("parentId", parent != null ? parent.getId() : null);
        return result;
    }

    public Map<String, Object> getAccount(Long accountId) {
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.SYS_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr) && !UserRole.PRODUCT_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only SYS_ADMIN, GROUP_ADMIN or PRODUCT_ADMIN can get accounts");
        }

        AccountEntity account = accountRepository.findById(accountId) 
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("id", account.getId());
        result.put("name", account.getName());
        result.put("nodeType", account.getNodeType().toString());
        result.put("parentId", account.getParent() != null ? account.getParent().getId() : null);
        
        
        List<AccountEntity> path = accountRepository.findPathByAccountId(accountId);

        List<Map<String, Object>> pathList = path.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("name", a.getName());
            return map;
        }).collect(java.util.stream.Collectors.toList());

        result.put("path", pathList);

        return result;
    }

    public List<Map<String, Object>> getAccountAccounts(Long accountId) {
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.SYS_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr) && !UserRole.PRODUCT_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only SYS_ADMIN, GROUP_ADMIN or PRODUCT_ADMIN can get accounts");
        }
        List<AccountEntity> accounts = accountRepository.findByParentId(accountId);

        return accounts.stream().map(account -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", account.getId());
            map.put("name", account.getName());
            map.put("nodeType", account.getNodeType().toString());
            map.put("parentId", account.getParent() != null ? account.getParent().getId() : null);
            map.put("clientId", account.getClient() != null ? account.getClient().getId() : null);
            map.put("tid", account.getTenant() != null ? account.getTenant().getId() : null);
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Получить иерархию аккаунтов
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAccountsHierarchy() {
        UserDetailsImpl currentUser = getCurrentUser();

        List<AccountEntity> accounts = accountRepository.findByTenantCodeAndClientId(
                currentUser.getTenantCode(),
                currentUser.getClientId()
        );

        return accounts.stream().map(account -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", account.getId());
            map.put("name", account.getName());
            map.put("nodeType", account.getNodeType().toString());
            map.put("parentId", account.getParent() != null ? account.getParent().getId() : null);
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * TNT_ADMIN / GROUP_ADMIN: Обновление роли на продукт
     */
    @Transactional
    public ProductRoleEntity updateProductRole(Long productRoleId, Boolean canRead, Boolean canQuote,
                                               Boolean canPolicy, Boolean canAddendum, Boolean canCancel,
                                               Boolean canProlong) {
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        if (!UserRole.TNT_ADMIN.getValue().equals(userRoleStr) && !UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can update product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        // Проверка: role должен быть в том же tenant
        if (!role.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot update role from different tenant");
        }

        // Для GROUP_ADMIN - дополнительная проверка на client
        if (UserRole.GROUP_ADMIN.getValue().equals(userRoleStr)) {
            if (!role.getClient().getId().equals(currentUser.getClientId())) {
                throw new ForbiddenException("GROUP_ADMIN can only update roles within their client");
            }
        }

        // Обновление permissions
        if (canRead != null) role.setCanRead(canRead);
        if (canQuote != null) role.setCanQuote(canQuote);
        if (canPolicy != null) role.setCanPolicy(canPolicy);
        if (canAddendum != null) role.setCanAddendum(canAddendum);
        if (canCancel != null) role.setCanCancel(canCancel);
        if (canProlong != null) role.setCanProlongate(canProlong);

        return productRoleRepository.save(role);
    }

    /**
     * Получить все роли на продукты для аккаунта
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getProductRolesByAccount(Long accountId) {
        UserDetailsImpl currentUser = getCurrentUser();

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        // Проверка: аккаунт должен быть в том же tenant
        if (!account.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot access account from different tenant");
        }

        List<ProductRoleEntity> roles = productRoleRepository.findByAccountId(accountId);

        return roles.stream().map(role -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", role.getId());
            map.put("roleProductId", role.getRoleProductId());
            map.put("roleAccountId", role.getRoleAccount().getId());
            map.put("canRead", role.getCanRead());
            map.put("canQuote", role.getCanQuote());
            map.put("canPolicy", role.getCanPolicy());
            map.put("canAddendum", role.getCanAddendum());
            map.put("canCancel", role.getCanCancel());
            map.put("canProlongate", role.getCanProlongate());
            return map;
        }).collect(java.util.stream.Collectors.toList());
    }

}


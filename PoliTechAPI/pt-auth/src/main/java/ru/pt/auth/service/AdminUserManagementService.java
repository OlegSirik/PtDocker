package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.*;
import ru.pt.auth.repository.*;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

/**
 * Сервис для управления администраторами и пользователями
 * с проверкой прав доступа на основе ролей
 */
@Service
public class AdminUserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserManagementService.class);

    private final TenantRepository tenantRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final ProductRoleRepository productRoleRepository;
    private final SecurityContextHelper securityContextHelper;

    public AdminUserManagementService(
            TenantRepository tenantRepository,
            ClientRepository clientRepository,
            AccountRepository accountRepository,
            LoginRepository loginRepository,
            AccountLoginRepository accountLoginRepository,
            ProductRoleRepository productRoleRepository,
            SecurityContextHelper securityContextHelper) {
        this.tenantRepository = tenantRepository;
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.productRoleRepository = productRoleRepository;
        this.securityContextHelper = securityContextHelper;
    }

    // ========== TENANT MANAGEMENT (SYS_ADMIN only) ==========

    /**
     * SYS_ADMIN: Создание нового tenant
     */
    @Transactional
    public TenantEntity createTenant(String tenantName) {
        // Проверка прав: только SYS_ADMIN
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can create tenants");
        }

        // Проверка уникальности имени
        if (tenantRepository.findByName(tenantName).isPresent()) {
            throw new BadRequestException("Tenant with name '" + tenantName + "' already exists");
        }

        TenantEntity tenant = new TenantEntity();
        tenant.setName(tenantName);
        tenant.setDeleted(false);

        TenantEntity saved = tenantRepository.save(tenant);
        logger.info("Tenant '{}' created by SYS_ADMIN", tenantName);
        return saved;
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     */
    @Transactional
    public void deleteTenant(Long tenantId) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can delete tenants");
        }

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found with ID: " + tenantId));

        tenant.setDeleted(true);
        tenantRepository.save(tenant);
        logger.info("Tenant '{}' deleted by SYS_ADMIN", tenant.getName());
    }

    // ========== TNT_ADMIN MANAGEMENT (SYS_ADMIN only) ==========

    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя для tenant
     */
    @Transactional
    public AccountLoginEntity createTntAdmin(Long tenantId, String userLogin, String userName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can create TNT_ADMIN users");
        }

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Проверка уникальности логина в tenant
        if (loginRepository.findByUserLogin(userLogin).isPresent()) {
            throw new BadRequestException("Login '" + userLogin + "' already exists");
        }

        // Создать Login
        LoginEntity login = new LoginEntity();
        login.setTenant(tenant);
        login.setUserLogin(userLogin);
        LoginEntity savedLogin = loginRepository.save(login);

        // Создать или получить TENANT account
        AccountEntity tenantAccount = getOrCreateTenantAccount(tenant);

        // Создать Client для этого tenant
        ClientEntity client = createDefaultClient(tenant);

        // Создать AccountLogin с ролью TNT_ADMIN
        return createAccountLogin(savedLogin, client, tenantAccount, "TNT_ADMIN");
    }

    /**
     * SYS_ADMIN: Удаление TNT_ADMIN пользователя
     */
    @Transactional
    public void deleteTntAdmin(Long accountLoginId) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can delete TNT_ADMIN users");
        }

        AccountLoginEntity accountLogin = accountLoginRepository.findById(accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        accountLoginRepository.deleteById(accountLoginId);
        logger.info("TNT_ADMIN user deleted: {}", accountLogin.getUserLogin());
    }

    // ========== GROUP_ADMIN MANAGEMENT (TNT_ADMIN only) ==========

    /**
     * TNT_ADMIN: Создание GROUP_ADMIN пользователя
     */
    @Transactional
    public AccountLoginEntity createGroupAdmin(String userLogin, String userName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can create GROUP_ADMIN users");
        }

        Long currentTenantId = currentUser.getTenantId();
        Long currentClientId = currentUser.getClientId();

        TenantEntity tenant = tenantRepository.findById(currentTenantId)
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
        login.setUserLogin(userLogin);
        LoginEntity savedLogin = loginRepository.save(login);

        // Создать или получить GROUP account
        AccountEntity groupAccount = createGroupAccount(tenant, client, userName);

        // Создать AccountLogin с ролью GROUP_ADMIN
        return createAccountLogin(savedLogin, client, groupAccount, "GROUP_ADMIN");
    }

    /**
     * TNT_ADMIN: Удаление GROUP_ADMIN пользователя
     */
    @Transactional
    public void deleteGroupAdmin(Long accountLoginId) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can delete GROUP_ADMIN users");
        }

        AccountLoginEntity accountLogin = accountLoginRepository.findById(accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        // Проверка: GROUP_ADMIN должен быть в том же tenant
        if (!accountLogin.getTenant().getId().equals(currentUser.getTenantId())) {
            throw new ForbiddenException("Cannot delete GROUP_ADMIN from different tenant");
        }

        accountLoginRepository.deleteById(accountLoginId);
        logger.info("GROUP_ADMIN user deleted: {}", accountLogin.getUserLogin());
    }

    // ========== PRODUCT_ADMIN MANAGEMENT (GROUP_ADMIN only) ==========

    /**
     * GROUP_ADMIN: Создание PRODUCT_ADMIN пользователя
     */
    @Transactional
    public AccountLoginEntity createProductAdmin(String userLogin, String userName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"GROUP_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only GROUP_ADMIN can create PRODUCT_ADMIN users");
        }

        Long currentTenantId = currentUser.getTenantId();
        Long currentClientId = currentUser.getClientId();

        TenantEntity tenant = tenantRepository.findById(currentTenantId)
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
        login.setUserLogin(userLogin);
        LoginEntity savedLogin = loginRepository.save(login);

        // Создать или получить PRODUCT account (подгруппа)
        AccountEntity productAccount = createProductAccount(tenant, client, userName);

        // Создать AccountLogin с ролью PRODUCT_ADMIN
        return createAccountLogin(savedLogin, client, productAccount, "PRODUCT_ADMIN");
    }

    /**
     * GROUP_ADMIN: Редактирование PRODUCT_ADMIN пользователя
     */
    @Transactional
    public AccountLoginEntity updateProductAdmin(Long accountLoginId, String newUserRole) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"GROUP_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only GROUP_ADMIN can edit PRODUCT_ADMIN users");
        }

        AccountLoginEntity accountLogin = accountLoginRepository.findById(accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        // Проверка: PRODUCT_ADMIN должен быть в той же группе
        if (!accountLogin.getClient().getId().equals(currentUser.getClientId())) {
            throw new ForbiddenException("Cannot edit PRODUCT_ADMIN from different group");
        }

        accountLogin.setUserRole(newUserRole);
        return accountLoginRepository.save(accountLogin);
    }

    // ========== PRODUCT_ROLES MANAGEMENT (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN: Выдача роли на продажу продукта GROUP_ADMIN'у
     */
    @Transactional
    public ProductRoleEntity assignProductRole(Long accountId, Long roleProductId, Boolean canRead,
                                               Boolean canQuote, Boolean canPolicy, Boolean canAddendum,
                                               Boolean canCancel, Boolean canProlong) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can assign product roles");
        }

        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        // Проверка: account должен быть в том же tenant
        if (!account.getTenant().getId().equals(currentUser.getTenantId())) {
            throw new ForbiddenException("Cannot assign role to account in different tenant");
        }

        // Создать ProductRole
        ProductRoleEntity role = new ProductRoleEntity();
        role.setTenant(account.getTenant());
        role.setClient(account.getClient());
        role.setAccount(account);
        role.setRoleProductId(roleProductId);
        role.setRoleAccount(account); // Роль привязана к account который получает доступ
        role.setCanRead(canRead != null && canRead);
        role.setCanQuote(canQuote != null && canQuote);
        role.setCanPolicy(canPolicy != null && canPolicy);
        role.setCanAddendum(canAddendum != null && canAddendum);
        role.setCanCancel(canCancel != null && canCancel);
        role.setCanProlongate(canProlong != null && canProlong);

        return productRoleRepository.save(role);
    }

    /**
     * TNT_ADMIN: Отзыв роли на продажу продукта
     */
    @Transactional
    public void revokeProductRole(Long productRoleId) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can revoke product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        // Проверка: role должен быть в том же tenant
        if (!role.getTenant().getId().equals(currentUser.getTenantId())) {
            throw new ForbiddenException("Cannot revoke role from different tenant");
        }

        productRoleRepository.deleteById(productRoleId);
        logger.info("ProductRole revoked");
    }

    // ========== HELPER METHODS ==========

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
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

    private ClientEntity createDefaultClient(TenantEntity tenant) {
        ClientEntity client = new ClientEntity();
        client.setTenant(tenant);
        client.setClientId("default-" + tenant.getId());
        client.setName("Default Client");
        client.setDeleted(false);
        return clientRepository.save(client);
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
        productAccount.setNodeType(AccountNodeType.ADMIN);
        productAccount.setName(productName);
        return accountRepository.save(productAccount);
    }

    private AccountLoginEntity createAccountLogin(LoginEntity login, ClientEntity client,
                                                   AccountEntity account, String role) {
        AccountLoginEntity accountLogin = new AccountLoginEntity();
        accountLogin.setTenant(login.getTenant());
        accountLogin.setClient(client);
        accountLogin.setAccount(account);
        accountLogin.setLogin(login);
        accountLogin.setUserLogin(login.getUserLogin());
        accountLogin.setUserRole(role);
        accountLogin.setDefault(true);
        return accountLoginRepository.save(accountLogin);
    }
}


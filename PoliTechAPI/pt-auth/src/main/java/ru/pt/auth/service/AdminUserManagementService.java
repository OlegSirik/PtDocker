package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Tuple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.*;
import ru.pt.auth.repository.*;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.model.AdminResponse;
import ru.pt.auth.utils.ClientMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.auth.entity.ClientConfigurationEntity;



/**
 * Сервис для управления администраторами и пользователями
 * с проверкой прав доступа на основе ролей
 */
@Service
public class AdminUserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserManagementService.class);

    private final TenantService tenantService;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    
    private final ProductRoleRepository productRoleRepository;
    private final SecurityContextHelper securityContextHelper;

    private final ClientMapper clientMapper = new ClientMapper();

    public AdminUserManagementService(
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

    // ========== TENANT MANAGEMENT (SYS_ADMIN only) ==========

    public List<TenantEntity> getTenants() {
        return tenantService.findAll();
    }

    /**
     * SYS_ADMIN: Создание нового tenant
     */
    @Transactional
    public TenantEntity createTenant(TenantEntity tenantDto) {

        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can create tenants");
        }

        TenantEntity tenant = new TenantEntity();

        tenant.setName(tenantDto.getName());
        tenant.setCode(tenantDto.getCode());
        tenant.setDeleted(false);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());

        // Проверка уникальности кода
        if (tenantService.findByName(tenant.getCode()).isPresent()) {
            throw new BadRequestException("Tenant with name '" + tenant.getCode() + "' already exists");
        }

        TenantEntity savedTenant = tenantService.save(tenant);

        AccountEntity account = new AccountEntity();
        account.setId(savedTenant.getId());
        account.setTenant(savedTenant);
//        account.setClient(client);
        account.setNodeType(AccountNodeType.TENANT);
        account.setName(tenant.getName());
//        account.setParent(tenantAccount);
        AccountEntity tenantAccount = accountRepository.save(account);

        logger.info("Tenant '{}' created by SYS_ADMIN", tenant.getName());

        ClientEntity client = createDefaultClient(tenant);

/*         
        Client client = new Client();
        client.setTid(tenant.getId());
        client.setClientId("SYS");
        client.setName("Default Admin App Client");
        client.setIsDeleted(false);
        Client savedClient = createClient(client);

        AccountEntity clientAccount = new AccountEntity();
        clientAccount.setId(client.getId());
        clientAccount.setTenant(tenant);
        clientAccount.setClient(client);
        clientAccount.setNodeType(AccountNodeType.CLIENT);
        clientAccount.setName(client.getName());
        clientAccount.setParent(tenantAccount);
        clientAccount = accountRepository.save(clientAccount);
*/

        return tenant;
    }

    /**
     * SYS_ADMIN: Обновление tenant
     */
    @Transactional
    public TenantEntity updateTenant(TenantEntity tenantIn) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can update tenants");
        }

        TenantEntity tenant = tenantService.findByCode(tenantIn.getCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Пока только название можно поменять 
        tenant.setName(tenantIn.getName());
        return tenantService.save(tenant);
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     */
    @Transactional
    public void deleteTenant(String tenantCode) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can delete tenants");
        }

        TenantEntity tenant = tenantService.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant not found with ID: " + tenantCode));

        tenant.setDeleted(true);
        tenantService.save(tenant);
        logger.info("Tenant '{}' deleted by SYS_ADMIN", tenant.getName());
    }

    // ========== SYS_ADMIN MANAGEMENT (SYS_ADMIN only) ==========

    public List<AdminResponse> getSysAdmins() {
        List<Tuple> result = accountLoginRepository.findByTenantAndUserRoleFull("SYS", "SYS_ADMIN");

        List<AdminResponse> responses = result.stream()
        .map(tuple -> new AdminResponse(
        tuple.get("id", Long.class),
        tuple.get("tid", Long.class),
        "SYS", //tuple.get("tenantCode", String.class),
        tuple.get("clientId", Long.class),
        tuple.get("accountId", Long.class),
        tuple.get("userLogin", String.class),
        tuple.get("userRole", String.class),
        tuple.get("fullName", String.class),
        tuple.get("position", String.class)
    ))
    .collect(Collectors.toList());
        return responses;
    }

        /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя для tenant
     */
    @Transactional
    public AccountLoginEntity createSysAdmin(
                String tenantCode,
                String userLogin,
                String userName) {

            /* 
            Проверка кода tenant для создания SYS_ADMIN пользователя
            Только для tenant с кодом SYS можно создавать SYS_ADMIN пользователя
            */
            if (!"SYS".equals(tenantCode)) {
                throw new BadRequestException("Wrong tenant code: " + tenantCode + " for SYS_ADMIN creation");
            }
            // Проверка прав
            UserDetailsImpl currentUser = getCurrentUser();
            if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
                throw new ForbiddenException("Only SYS_ADMIN can create TNT_ADMIN users");
            }
    
            TenantEntity tenant = tenantService.findByCode(tenantCode)
                    .orElseThrow(() -> new NotFoundException("Tenant not found"));
    
            // Проверка уникальности логина в tenant
            LoginEntity savedLogin = loginRepository.findByTenantCodeAndUserLogin("SYS", userLogin)
                .orElseGet(() -> {
                    LoginEntity newLogin = new LoginEntity();
                    newLogin.setTenant(tenant);
                    newLogin.setFullName(userName);
                    newLogin.setPosition("SYS_ADMIN");
                    newLogin.setUserLogin(userLogin);
                    return loginRepository.save(newLogin);
                });
    
            // Создать или получить TENANT account
            AccountEntity tenantAccount = getOrCreateTenantAccount(tenant);
    
            // Создать Client для этого tenant
            ClientEntity client = createDefaultClient(tenant);
    
            // Создать AccountLogin с ролью TNT_ADMIN
            return createAccountLogin(savedLogin, client, tenantAccount, "SYS_ADMIN");
        }
    

    // ========== TNT_ADMIN MANAGEMENT (SYS_ADMIN only) ==========

    public List<AdminResponse> getTntAdmins(String tenantCode) {
        List<Tuple> result = accountLoginRepository.findByTenantAndUserRoleFull(tenantCode, "TNT_ADMIN");

        List<AdminResponse> responses = result.stream()
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
        return responses;
    }

    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя для tenant
     */
    @Transactional
    public AccountLoginEntity createTntAdmin(
            String tenantCode,
            String fullName,
            String userLogin,
            String userName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can create TNT_ADMIN users");
        }

        TenantEntity tenant = tenantService.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant not found"));


                // Проверка уникальности логина в tenant
        LoginEntity savedLogin = loginRepository.findByTenantCodeAndUserLogin(tenantCode, userLogin)
                .orElseGet(() -> {
                    LoginEntity newLogin = new LoginEntity();
                    newLogin.setTenant(tenant);
                    newLogin.setFullName(userName);
                    newLogin.setPosition("TNT_ADMIN");
                    newLogin.setUserLogin(userLogin);
                    return loginRepository.save(newLogin);
                });



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
    public AccountLoginEntity createGroupAdmin(String userLogin, String userName, String fullName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole())) {
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
        if (!accountLogin.getTenant().getId().equals(currentUser.getTenantCode())) {
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
    public AccountLoginEntity createProductAdmin(String userLogin, String userName, String fullName) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"GROUP_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only GROUP_ADMIN can create PRODUCT_ADMIN users");
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
     * TNT_ADMIN / GROUP_ADMIN: Выдача роли на продажу продукта
     */
    @Transactional
    public ProductRoleEntity assignProductRole(Long accountId, Long roleProductId, Long roleAccountId,
                                               Boolean canRead, Boolean canQuote, Boolean canPolicy,
                                               Boolean canAddendum, Boolean canCancel, Boolean canProlong) {
        // Проверка прав
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"GROUP_ADMIN".equals(currentUser.getUserRole())) {
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
        if ("GROUP_ADMIN".equals(currentUser.getUserRole())) {
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
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"GROUP_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can revoke product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        // Проверка: role должен быть в том же tenant
        if (!role.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot revoke role from different tenant");
        }

        // Для GROUP_ADMIN - дополнительная проверка на client
        if ("GROUP_ADMIN".equals(currentUser.getUserRole())) {
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
        String defClientId = "SYS";

        List<ClientEntity> clients = clientRepository.findByTenantCodeActive(tenant.getCode()).stream()
            .filter(c -> defClientId.equals(c.getClientId()))
            .toList();

        if (!clients.isEmpty()) { return clients.get(0); }

        AccountEntity tenantAccount = accountRepository.findByTenantId(tenant.getId())
            .orElseThrow(() -> new NotFoundException("Tenant account not found for tenant id: " + tenant.getId()));

        ClientEntity client = new ClientEntity();
        client.setTenant(tenant);
        client.setClientId(defClientId);
        client.setName("Default Admin App Client");
        client.setDeleted(false);
        ClientEntity saved = clientRepository.save(client);

        AccountEntity clientAccount = new AccountEntity();
        clientAccount.setId(client.getId());
        clientAccount.setTenant(tenant);
        clientAccount.setClient(client);
        clientAccount.setNodeType(AccountNodeType.CLIENT);
        clientAccount.setName(client.getName());
        clientAccount.setParent(tenantAccount);
        clientAccount = accountRepository.save(clientAccount);
    
        return saved;
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

    // ========== CLIENT MANAGEMENT (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN: Создание нового клиента
     */
    @Transactional
    public Client createClient(Client client) {

        UserDetailsImpl currentUser = getCurrentUser();

        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can create clients");
        }

        TenantEntity tenant = tenantService.findByCode(currentUser.getTenantCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Проверка уникальности clientId
        if (clientRepository.findByClientIdandTenantCode(client.getClientId(), currentUser.getTenantCode()).isPresent()) {
            throw new BadRequestException("Client with ID '" + client.getClientId() + "' already exists");
        }

        client.setTid(tenant.getId());

        if (client.getClientConfiguration() == null) {
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            client.setClientConfiguration(clientConfiguration);
        }

        ClientEntity clientEntity = clientMapper.toEntity(client);
        ClientEntity saved = clientRepository.save(clientEntity);

        AccountEntity tenantAccount = accountRepository.findByTenantId(tenant.getId())
            .orElseThrow(() -> new NotFoundException("Tenant account not found for tenant id: " + tenant.getId()));

        AccountEntity account = new AccountEntity();
        account.setTenant(tenant);
        account.setClient(clientEntity);
        account.setNodeType(AccountNodeType.CLIENT);
        account.setName(client.getName());
        account.setParent(tenantAccount);
        AccountEntity savedAccount = accountRepository.save(account);
// default account
        AccountEntity defAccount = new AccountEntity();
        defAccount.setTenant(tenant);
        defAccount.setClient(clientEntity);
        defAccount.setNodeType(AccountNodeType.ACCOUNT);
        defAccount.setName(client.getName() + " default SALE account");
        defAccount.setParent(savedAccount);
        AccountEntity savedDefAccount = accountRepository.save(defAccount);

        saved.setDefaultAccountId(savedDefAccount.getId());
        clientRepository.save(saved);
        
        Client clientDto = clientMapper.toDto(saved);
        clientDto.setClientAccountId(savedAccount.getId());
        return clientDto;
       
    }

    @Transactional
    public Client updateClient(Client client) {
        UserDetailsImpl currentUser = getCurrentUser();

        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can create clients");
        }

        TenantEntity tenant = tenantService.findByCode(currentUser.getTenantCode())
            .orElseThrow(() -> new NotFoundException("Tenant not found"));

        ClientEntity clientToUpdate = clientRepository.findById(client.getId())
            .orElseThrow(() -> new NotFoundException("Client not found"));

        if ("TNT_ADMIN".equals(currentUser.getUserRole())) {
            // Если это sys_admin, то можно обновить любого клиента, если это tnt_admin то только из своего тенанта
            // tenant текущей учетки

            // Проверка, что текущий пользователь имеет доступ к этой записи    
            if (!clientToUpdate.getTenant().getCode().equals(tenant.getCode())) {
                throw new ForbiddenException("You do not have access to this tenant data");
            }
        }

        if (clientToUpdate.getClientId() != client.getClientId()) {
            // Проверка уникальности нового clientId
            ClientEntity oldClient = clientRepository.findByClientIdandTenantCode(client.getClientId(), currentUser.getTenantCode()).
                orElse(new ClientEntity());
                        
            if (oldClient.getId() != null && oldClient.getId() != client.getId()) {
                throw new BadRequestException("Client with ID '" + client.getClientId() + "' already exists");
            }
        }

        client.setUpdatedAt(LocalDateTime.now());
        client.setTid(tenant.getId());

        ClientEntity clientEntity = clientMapper.toEntity(client);
        ClientEntity saved = clientRepository.save(clientEntity);

        AccountEntity account = accountRepository.findCliensAccountByClientId(saved.getId())
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + saved.getClientId()));

    
        Client clientDto = clientMapper.toDto(saved);
        clientDto.setClientAccountId(account.getId());
        return clientDto;

        //return getClientById(client.getId());
//        return clientMapper.toDto(saved);
    }
    /**
     * TNT_ADMIN: Получить список всех клиентов
     */
    @Transactional(readOnly = true)
    public List<ClientEntity> listClients() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can list clients");
        }

        List<ClientEntity> clients = clientRepository.findBytenantCode(currentUser.getTenantCode());
        return clients;
    }

    /**
     * TNT_ADMIN: Получить клиента по ID
     */
    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can list clients");
        }

        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (!client.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot access client from different tenant");
        }

        AccountEntity account = accountRepository.findCliensAccountByClientId(id)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + id));

        
        Client clientDto = clientMapper.toDto(client);
        clientDto.setClientAccountId(account.getId());
        return clientDto;
    }

    // ========== ACCOUNT MANAGEMENT (GROUP_ADMIN, PRODUCT_ADMIN) ==========

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Создание аккаунта
     */
    @Transactional
    public Map<String, Object> createAccount(Long parentAccountId, String accountName, String nodeType) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole()) && !"GROUP_ADMIN".equals(currentUser.getUserRole()) && !"PRODUCT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only GROUP_ADMIN or PRODUCT_ADMIN can create accounts");
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
            accountNodeType = AccountNodeType.valueOf(nodeType.toUpperCase());
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
        if (!"SYS_ADMIN".equals(currentUser.getUserRole()) && !"GROUP_ADMIN".equals(currentUser.getUserRole()) && !"PRODUCT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only GROUP_ADMIN or PRODUCT_ADMIN can get accounts");
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
        if (!"SYS_ADMIN".equals(currentUser.getUserRole()) && !"GROUP_ADMIN".equals(currentUser.getUserRole()) && !"PRODUCT_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only GROUP_ADMIN or PRODUCT_ADMIN can get accounts");
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
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"GROUP_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN or GROUP_ADMIN can update product roles");
        }

        ProductRoleEntity role = productRoleRepository.findById(productRoleId)
                .orElseThrow(() -> new NotFoundException("ProductRole not found"));

        // Проверка: role должен быть в том же tenant
        if (!role.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot update role from different tenant");
        }

        // Для GROUP_ADMIN - дополнительная проверка на client
        if ("GROUP_ADMIN".equals(currentUser.getUserRole())) {
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


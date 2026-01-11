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
import ru.pt.api.dto.auth.AccountLogin;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.pt.auth.utils.AccountLoginMapper;

/**
 *  Сервис для управления администраторами - system, tenant, product
 *  Это необходимый минимум для работы через АПИ, без управления пользователями клиента.
 *  с проверкой прав доступа на основе ролей
 */
@Service
public class AdminUserManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserManagementService.class);

    private final TenantService tenantService;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final AccountLoginMapper accountLoginMapper;
    private final ProductRoleRepository productRoleRepository;
    private final SecurityContextHelper securityContextHelper;

    public AdminUserManagementService(  
            TenantService tenantService,
            ClientRepository clientRepository,
            AccountRepository accountRepository,
            LoginRepository loginRepository,
            AccountLoginRepository accountLoginRepository,
            AccountLoginMapper accountLoginMapper,
            ProductRoleRepository productRoleRepository,
            SecurityContextHelper securityContextHelper) {
        this.tenantService = tenantService;
        this.clientRepository = clientRepository;
        this.accountRepository = accountRepository;
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.accountLoginMapper = accountLoginMapper;
        this.productRoleRepository = productRoleRepository;
        this.securityContextHelper = securityContextHelper;
    }

    // Возвращает список админов с нужным типом и из нужного тенанта
    public List<AdminResponse> getAdmins(String tenantCode, UserRole role) {
        // для sys_admin он в заголовке
        String requestTenantCode = checkPermitionAndGetTenantCode(tenantCode);

        List<Tuple> result = accountLoginRepository.findByTenantAndUserRoleFull(requestTenantCode, role.getValue());
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

    @Transactional
    public AdminResponse createAdmin(String tenantCode, String authClientId,String userLogin, UserRole role) {
        String currentTenantCode = checkPermitionAndGetTenantCode(tenantCode);

        AccountEntity adminAccount = accountRepository.findAdminAccount(currentTenantCode, authClientId, AccountNodeType.fromString(role.getValue()))
            .orElseThrow(() -> new NotFoundException("Client account not found " + role.getValue()));

        // Логин должен быть создан в тенанте заранее
        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(currentTenantCode, userLogin)
            .orElseThrow(() -> new NotFoundException("Login not found"));

        AccountLoginEntity accountLogin = null;

        if (role == UserRole.SYS_ADMIN) {
            if (!userIsSysAdmin()) {
                throw new ForbiddenException("Only SYS_ADMIN can create SYS_ADMIN users");
            }
        
            if ( !tenantCode.equals(TenantEntity.SYS_TENANT_CODE) ) {
                throw new BadRequestException("Tenant code is required");
            }
    
            accountLogin = AccountLoginEntity.createSysAdmin(adminAccount, userLogin);
        } else if (role == UserRole.TNT_ADMIN) {
             accountLogin = AccountLoginEntity.createTntAdmin(adminAccount, userLogin);
        } else if (role == UserRole.PRODUCT_ADMIN) {
             accountLogin = AccountLoginEntity.createProductAdmin(adminAccount, userLogin);
        } else {
            throw new BadRequestException("Invalid role: " + role.getValue());
        }
        
        accountLogin.setLogin(loginEntity);

        AccountLoginEntity saved = accountLoginRepository.save(accountLogin);
    
        return fromEntity(saved);
    }
    
    /**
     * Unified method to delete admin by ID.
     * Determines the role from the admin record and calls the appropriate delete method.
     * 
     * @param tenantCode the tenant code
     * @param accountLoginId the account login ID to delete
     */
    @Transactional
    public void deleteAdmin(String tenantCode, Long accountLoginId) {

        // Проверить что это тенант админ в своем тенанте или сис админ имперсонированный
        String currentTenantCode = checkPermitionAndGetTenantCode(tenantCode);

        // Проверить что ИД из тенантна
        AccountLoginEntity accountLogin = accountLoginRepository.findByTenantCodeAndLoginEntityId(currentTenantCode, accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        AccountNodeType role = accountLogin.getAccount().getNodeType();
        if (role == AccountNodeType.SYS_ADMIN) {    
            if (!tenantCode.equals(TenantEntity.SYS_TENANT_CODE)) {
                throw new BadRequestException("Tenant code is required");
            }
            // Проверка прав
            if ( !userIsSysAdmin() ) {
                throw new ForbiddenException("Only SYS_ADMIN can delete SYS_ADMIN users");
            }
            logger.info("SYS_ADMIN user deleted: {}", accountLogin.getUserLogin());
        } else if (role == AccountNodeType.TNT_ADMIN || role == AccountNodeType.PRODUCT_ADMIN) {
            logger.info("{} user deleted: {}", role.getValue(), accountLogin.getUserLogin());
        } else {
            throw new BadRequestException("Cannot delete admin with role: " + role.getValue());
        }

        accountLoginRepository.deleteById(accountLoginId);
    }


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
        /* Только для пользователя SYS, для работы в других тенантах */
        UserDetailsImpl currentUser = getCurrentUser();
        if ( !getUserTenantCode().equals(TenantEntity.SYS_TENANT_CODE) ) {
            return getUserTenantCode();
        }
        return currentUser.getImpersonatedTenantCode();
    }


    private boolean userIsSysAdmin() {
        UserDetailsImpl currentUser = getCurrentUser();
        String userRoleStr = currentUser.getUserRole();
        String sysAdminRole = UserRole.SYS_ADMIN.getValue();
        return sysAdminRole.equals(userRoleStr);
    }

    private boolean userIsTntAdmin(String tenantCode) {
        UserDetailsImpl currentUser = getCurrentUser();
        return UserRole.TNT_ADMIN.getValue().equals(currentUser.getUserRole()) && 
               tenantCode != null && tenantCode.equals(currentUser.getTenantCode());
    }

    private String checkPermitionAndGetTenantCode(String tenantCode) {

        if ( userIsTntAdmin(tenantCode) ) 
        {
            return tenantCode;
        }
        if ( userIsSysAdmin() )
        {
            String currentTenantCode = getUserImpersonatedTenantCode();
            if (currentTenantCode == null) {
                throw new ForbiddenException("SYS ADMIN must be impersonated");
            }
            return currentTenantCode;
        }
        throw new ForbiddenException("Only SYS_ADMIN or TNT_ADMIN can access other tenants");
    }

    private AdminResponse fromEntity(AccountLoginEntity accountLogin) {
        return new AdminResponse(
            accountLogin.getId(),
            accountLogin.getTenant().getId(),
            accountLogin.getTenant().getCode(),
            accountLogin.getClient() != null ? accountLogin.getClient().getId() : null,
            accountLogin.getAccount().getId(),
            accountLogin.getUserLogin(),
            accountLogin.getAccount().getNodeType().getValue(),
            accountLogin.getLogin().getFullName(),
            accountLogin.getLogin().getPosition()
        );
    }
}


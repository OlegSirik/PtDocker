package ru.pt.auth.service.admin;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.auth.AccountLoginService;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.auth.entity.*;
import ru.pt.auth.model.AdminResponse;
import ru.pt.auth.repository.*;
import ru.pt.auth.service.AccountServiceImpl;
import ru.pt.auth.service.TenantService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Unified service for managing admins (SYS, TNT, PRODUCT, CLIENT, GROUP) and related operations.
 * Merged from AdminUserManagementService and ClientAdminsManagementService.
 */
@Service
@RequiredArgsConstructor
public class AdminManagementService {

    private static final Logger logger = LoggerFactory.getLogger(AdminManagementService.class);

    private final TenantService tenantService;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final AdminPermissionHelper adminPermissionHelper;
    private final AuthorizationService authService;
    private final AccountServiceImpl accountService;
    private final AccountLoginService accountLoginService;

    // ========== SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN ==========

    public List<AdminResponse> getAdmins(String tenantCode, UserRole role) {
        String requestTenantCode = adminPermissionHelper.checkPermissionAndGetTenantCode(tenantCode);
        List<Tuple> result = accountLoginRepository.findByTenantAndUserRoleFull(requestTenantCode, role.getValue());
        return result.stream()
                .map(tuple -> new AdminResponse(
                        tuple.get("id", Long.class),
                        tuple.get("tid", Long.class),
                        tenantCode,
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
    public AdminResponse createAdmin(String tenantCode, String authClientId, String userLogin, UserRole role) {
        String currentTenantCode = adminPermissionHelper.checkPermissionAndGetTenantCode(tenantCode);

        AccountEntity adminAccount = accountRepository.findAdminAccount(currentTenantCode, authClientId, AccountNodeType.fromString(role.getValue()))
                .orElseThrow(() -> new NotFoundException("Client account not found " + role.getValue()));

        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(currentTenantCode, userLogin)
                .orElseThrow(() -> new NotFoundException("Login not found"));

        AccountLoginEntity accountLogin;
        if (role == UserRole.SYS_ADMIN) {
            if (!adminPermissionHelper.userIsSysAdmin()) {
                throw new ForbiddenException("Only SYS_ADMIN can create SYS_ADMIN users");
            }
            if (!tenantCode.equals(TenantEntity.SYS_TENANT_CODE)) {
                throw new BadRequestException("Tenant code is required");
            }
            accountLogin = AccountLoginEntity.create(adminAccount, loginEntity);
        } else if (role == UserRole.TNT_ADMIN) {
            accountLogin = AccountLoginEntity.create(adminAccount, loginEntity);
        } else if (role == UserRole.PRODUCT_ADMIN) {
            accountLogin = AccountLoginEntity.create(adminAccount, loginEntity);
        } else {
            throw new BadRequestException("Invalid role: " + role.getValue());
        }

        accountLogin.setLogin(loginEntity);
        AccountLoginEntity saved = accountLoginRepository.save(accountLogin);
        return fromEntity(saved);
    }

    @Transactional
    public void deleteAdmin(String tenantCode, Long accountLoginId) {
        String currentTenantCode = adminPermissionHelper.checkPermissionAndGetTenantCode(tenantCode);

        AccountLoginEntity accountLogin = accountLoginRepository.findByTenantCodeAndLoginEntityId(currentTenantCode, accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        AccountNodeType role = accountLogin.getAccount().getNodeType();
        if (role == AccountNodeType.SYS_ADMIN) {
            if (!tenantCode.equals(TenantEntity.SYS_TENANT_CODE)) {
                throw new BadRequestException("Tenant code is required");
            }
            if (!adminPermissionHelper.userIsSysAdmin()) {
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

    // ========== CLIENT_ADMIN (TNT_ADMIN only) ==========

    public List<AdminResponse> getClientAdmins(String tenantCode, Long clientId) {
        String requestTenantCode = adminPermissionHelper.checkPermissionAndGetTenantCode(tenantCode);
        return getAdminsInternal(requestTenantCode, UserRole.CLIENT_ADMIN.getValue());
    }

    @Transactional
    public AccountLoginEntity createClientAdmin(String tenantCode, String authClientId, String userLogin) {
        String currentTenantCode = adminPermissionHelper.checkPermissionAndGetTenantCode(tenantCode);

        AccountEntity clientAccount = accountRepository.findClientAccount(currentTenantCode, authClientId)
                .orElseThrow(() -> new NotFoundException("Client account not found"));

        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(currentTenantCode, userLogin)
                .orElseThrow(() -> new NotFoundException("Login not found"));

        AccountLoginEntity accountLogin = AccountLoginEntity.create(clientAccount, loginEntity);
        accountLogin.setLogin(loginEntity);
        return accountLoginRepository.save(accountLogin);
    }

    @Transactional
    public void deleteClientAdmin(Long accountLoginId) {
        // TNT_ADMIN or SYS_ADMIN (impersonated) only
        String currentTenantCode = adminPermissionHelper.getUserEffectiveTenantCode();
        if (!adminPermissionHelper.userIsSysAdmin() && !adminPermissionHelper.userIsTntAdmin(currentTenantCode)) {
            throw new ForbiddenException("Only SYS_ADMIN or TNT_ADMIN can delete CLIENT_ADMIN users");
        }

        AccountLoginEntity accountLogin = accountLoginRepository.findByTenantCodeAndLoginEntityId(currentTenantCode, accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        accountLoginRepository.deleteById(accountLoginId);
        logger.info("CLIENT_ADMIN user deleted: {}", accountLogin.getUserLogin());
    }

    // ========== GROUP_ADMIN (TNT_ADMIN only) ==========

    @Transactional
    public AdminResponse createGroupAdmin(Long groupId, UserRole role, String userLogin) {
        
        var currentUser = adminPermissionHelper.getCurrentUser();
        String currentTenantCode = currentUser.getTenantCode();
        Long currentClientId = currentUser.getClientId();

        /* Есть доступ к этой группе */
        authService.check(currentUser, AuthZ.ResourceType.ACCOUNT, null, groupId, AuthZ.Action.MANAGE);

        /* У этого Account есть админская роль */
        AccountEntity adminAccount = accountRepository.findGroupAdminAccount(currentTenantCode, groupId, AccountNodeType.fromString(role.getValue()))
                .orElseThrow(() -> new BadRequestException("Admin account not found for group " + groupId));

        /* Есть такой логин */
        LoginEntity login = loginRepository.findByUserLogin(userLogin)
            .orElseThrow(() -> new BadRequestException("Login '" + userLogin + "' not found"));

        AccountLoginEntity accountLogin = AccountLoginEntity.create(adminAccount, login);
        accountLogin.setLogin(login);
        AccountLoginEntity saved = accountLoginRepository.save(accountLogin);

        logger.info("GROUP_ADMIN user created: {} for group '{}'", userLogin, groupId);
        return fromEntity(saved);
    }

    @Transactional
    public void deleteGroupAdmin(Long accountLoginId) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        //String currentTenantCode = currentUser.getTenantCode();
        //Long currentClientId = currentUser.getClientId();


        AccountLoginEntity accountLogin = accountLoginRepository.findById(accountLoginId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        /* Есть доступ к этой группе */
        authService.check(currentUser, AuthZ.ResourceType.ACCOUNT, accountLogin.getAccount().getId().toString(), accountLogin.getAccount().getId(), AuthZ.Action.MANAGE);

        accountLoginRepository.deleteById(accountLoginId);
        logger.info("GROUP_ADMIN user deleted: {}", accountLogin.getUserLogin());
    }

    /**
     * List GROUP_ADMIN users for a group account.
     * @param groupAccountId the GROUP account id (must be nodeType GROUP)
     */
    public List<AdminResponse> getGroupAdmins(Long groupAccountId) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        String currentTenantCode = currentUser.getTenantCode();
        Long currentClientId = currentUser.getClientId();

        AccountEntity groupAccount = accountRepository.findById(groupAccountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        /* Есть доступ к этой группе */
        authService.check(currentUser, AuthZ.ResourceType.ACCOUNT, groupAccount.getId().toString(), groupAccount.getId(), AuthZ.Action.MANAGE);

        List<AccountEntity> groupAdminAccounts = accountRepository.findByParentId(groupAccountId).stream()
                .filter(a -> a.getNodeType() == AccountNodeType.GROUP_ADMIN)
                .toList();

        return groupAdminAccounts.stream()
                .flatMap(ga -> accountLoginRepository.findByAccountEntityId(ga.getId()).stream())
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Add GROUP_ADMIN user to an existing group.
     * @param groupAccountId the GROUP account id (must be nodeType GROUP)
     * @param userLogin login that must exist in the tenant
     */
    @Transactional
    public AdminResponse addGroupAdminToGroup(Long groupAccountId, String userLogin) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        if (!UserRole.TNT_ADMIN.getValue().equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can add GROUP_ADMIN users");
        }

        String currentTenantCode = currentUser.getTenantCode();

        AccountEntity groupAccount = accountRepository.findById(groupAccountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (groupAccount.getNodeType() != AccountNodeType.GROUP) {
            throw new BadRequestException("Account must be of type GROUP, got: " + groupAccount.getNodeType());
        }

        if (!groupAccount.getTenant().getCode().equals(currentTenantCode)) {
            throw new ForbiddenException("Cannot add GROUP_ADMIN to group in different tenant");
        }

        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(currentTenantCode, userLogin)
                .orElseThrow(() -> new NotFoundException("Login not found"));

        AccountEntity groupAdminAccount = AccountEntity.createAccount(groupAccount, null, AccountNodeType.GROUP_ADMIN);
        groupAdminAccount.setId(accountRepository.getNextAccountId());
        accountRepository.save(groupAdminAccount);

        AccountLoginEntity accountLogin = AccountLoginEntity.create(groupAdminAccount, loginEntity);
        accountLogin.setLogin(loginEntity);
        AccountLoginEntity saved = accountLoginRepository.save(accountLogin);

        logger.info("GROUP_ADMIN user '{}' added to group {}", userLogin, groupAccountId);
        return fromEntity(saved);
    }

    // ========== HELPERS ==========

    private List<AdminResponse> getAdminsInternal(String tenantCode, String userRole) {
        List<Tuple> result = accountLoginRepository.findByTenantAndUserRoleFull(tenantCode, userRole);
        return result.stream()
                .map(tuple -> new AdminResponse(
                        tuple.get("id", Long.class),
                        tuple.get("tid", Long.class),
                        tenantCode,
                        tuple.get("clientId", Long.class),
                        tuple.get("accountId", Long.class),
                        tuple.get("userLogin", String.class),
                        tuple.get("userRole", String.class),
                        tuple.get("fullName", String.class),
                        tuple.get("position", String.class)
                ))
                .collect(Collectors.toList());
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

//--------------------------

    
/* 
    public AdminResponse grantRole(Long groupId, UserRole role, String userLogin) {
        
        Account account = accountService.getOrCreateRoleAccount(AccountNodeType.fromString(role.getValue()), userLogin, groupId);

        /* Есть такой логин
        LoginEntity login = loginRepository.findByUserLogin(userLogin)
            .orElseThrow(() -> new BadRequestException("Login '" + userLogin + "' not found"));

        AccountLoginService.create( account.id(), login);

    logger.info("GROUP_ADMIN user created: {} for group '{}'", userLogin, groupId);
    return fromEntity(saved);
}
*/

}

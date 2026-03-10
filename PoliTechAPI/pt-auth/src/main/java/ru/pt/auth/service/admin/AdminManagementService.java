package ru.pt.auth.service.admin;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.auth.entity.*;
import ru.pt.auth.model.AdminResponse;
import ru.pt.auth.repository.*;
import ru.pt.auth.service.AccountServiceImpl;

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

    private final AccountRepository accountRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final AdminPermissionHelper adminPermissionHelper;
    private final AuthorizationService authService;
    private final AccountServiceImpl accountService;
    
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

    private AccountLoginEntity linkUserToAccount(Long accountId, String userLogin) {
        var currentUser = adminPermissionHelper.getCurrentUser();
        authService.check(currentUser, AuthZ.ResourceType.ACCOUNT, accountId.toString(), accountId, AuthZ.Action.MANAGE);

        AccountEntity roleAccountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(roleAccountEntity.getTenant().getCode(), userLogin)
                .orElseThrow(() -> new NotFoundException("Login not found"));

        // Если уже есть привязка логина к этому аккаунту, возвращаем её
        var existing = accountLoginRepository.findByUserLoginAndAccountId(userLogin, accountId);
        if (existing.isPresent()) {
            return existing.get();
        }

        AccountLoginEntity accountLogin = AccountLoginEntity.create(roleAccountEntity, loginEntity);
        return accountLoginRepository.save(accountLogin);
    }

    /* Добавить логин к роли.  Находится узел с родителем groupId и ролью role. И к нему вязется логин.
       Если ролевого узла нет, но он допустим, то создается новый.
     */
    /**
     * Get the client "sys" account id for the tenant (parent of SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN role accounts).
     */
    private Long getClientSysAccountId(String tenantCode) {
        String currentTenantCode = adminPermissionHelper.checkPermissionAndGetTenantCode(tenantCode);
        return accountRepository.findClientAccount(currentTenantCode, ClientEntity.SYS_CLIENT_ID)
                .orElseThrow(() -> new NotFoundException("Client account not found for tenant: " + tenantCode))
                .getId();
    }


    private List<AdminResponse> getAccountUsers( Long accountId, UserRole role ) {

        /* Получить все подгруппы с нужным типом */
        List<AccountEntity> groupAdminAccounts = accountRepository.findByParentId(accountId).stream()
                .filter(a -> a.getNodeType() == AccountNodeType.fromString(role.getValue()))
                .toList();

        /* Для кадой подгруппы получить все учетки и собрать в одну коллекцию. Не супер но пока сойдет, потом переделать на SQL */
        return groupAdminAccounts.stream()
                .flatMap(ga -> accountLoginRepository.findByAccountEntityId(ga.getId()).stream())
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }


    public Long getSysAccountId () {
        var currentTenant = adminPermissionHelper.getUserEffectiveTenantCode();
        Long accountId = getClientSysAccountId(currentTenant);
        return accountId;
    }

    /* GET accounts/id/members?type= */
    @Transactional
    public List<AdminResponse> getAccountMembers(Long accountId, String role) {

        var currentUser = adminPermissionHelper.getCurrentUser();

        /* Есть доступ к этой группе */
        authService.check(currentUser, AuthZ.ResourceType.ACCOUNT, accountId.toString(), accountId, AuthZ.Action.LIST);

        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (AccountEntity.nodeTypeHasChildren(accountEntity.getNodeType())) {
            /* Это не терминальный аккаунт. Нужна роль */
            if (role == null || role.isEmpty()) {
                throw new BadRequestException("Не указана роль для фильтрации пользователей");
            }
            return getAccountUsers(accountId, UserRole.valueOf(role));
        } else {
            if (role != null && !role.isEmpty()) {
                throw new BadRequestException("Для этого account роль не применима");
            }
            return accountLoginRepository.findByAccountEntityId(accountId).stream()
                    .map(this::fromEntity)
                    .collect(Collectors.toList());
        }
    }

    /* POST accounts/id/members */
    @Transactional
    public AdminResponse setAccountMember(Long accountId, String role, String login) {
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));

        if (AccountEntity.nodeTypeHasChildren(accountEntity.getNodeType())) {
            /* Это не терминальный аккаунт. Нужна роль */
            if (role == null || role.isEmpty()) {
                throw new BadRequestException("Не указана роль для пользователя");
            }
            Account roleAccount = accountService.getOrCreateRoleAccount(AccountNodeType.fromString(role), role.toUpperCase(), accountId);

            var saved = linkUserToAccount(roleAccount.id(), login);

            logger.info("user '{}' added to group {}", login, accountId);
            return fromEntity(saved);
        } else {
            if (role != null && !role.isEmpty()) {
                throw new BadRequestException("Для этого account роль не применима");
            }
            var saved = linkUserToAccount(accountId, login);

            logger.info("user '{}' added to account {}", login, accountId);
            return fromEntity(saved);
        }        
    }

    /* DELETE accounts/id/members/id */
    @Transactional
    public void deleteAccountMember(Long accountId, Long memberId) {
        if (accountId == null || memberId == null) {
            throw new BadRequestException("member not found for account");
        }

        var currentUser = adminPermissionHelper.getCurrentUser();

        /* Есть доступ к этому узлу */
        authService.check(currentUser, AuthZ.ResourceType.ACCOUNT, accountId.toString(), accountId, AuthZ.Action.MANAGE);

        AccountLoginEntity alEntity = accountLoginRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("AccountLogin not found"));

        if (!alEntity.getAccount().getId().equals(accountId)) {
            throw new BadRequestException("member not found for account");
        }

        accountLoginRepository.deleteById(memberId);
    }

}

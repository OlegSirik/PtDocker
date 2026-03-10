package ru.pt.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.AccountTokenEntity;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.AccountTokenRepository;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.repository.ProductRoleRepository;
import ru.pt.auth.security.context.RequestContext;
import ru.pt.auth.service.ClientService;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.entity.AccountEntity;

/**
 * Реализация UserDetailsService для Spring Security.
 * Загружает пользователя по логину из базы данных.
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AccountTokenRepository accountTokenRepository;

    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final AccountRepository accountRepository;
    private final ProductRoleRepository productRoleRepository;
    private final ClientService clientService;
    private final RequestContext requestContext;
    
    public UserDetailsServiceImpl(LoginRepository loginRepository,
                                  AccountLoginRepository accountLoginRepository,
                                  AccountRepository accountRepository,
                                  ProductRoleRepository productRoleRepository,
                                  ClientService clientService,
                                  RequestContext requestContext, AccountTokenRepository accountTokenRepository) {
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.accountRepository = accountRepository;
        this.productRoleRepository = productRoleRepository;
        this.clientService = clientService;
        this.requestContext = requestContext;
        this.accountTokenRepository = accountTokenRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //username это acc_account.id 
        // id анонимной учетки с правами

        Long accountId = Long.parseLong(username);
        String tenantCode = requestContext.getTenant();
        // tenant если есть то он уже проверен.
        if (tenantCode == null || tenantCode.isEmpty()) {
            throw new IllegalStateException("TenantContext not set");
        }

        String authClientId = requestContext.getClient();
        if (authClientId == null || authClientId.isEmpty()) {
            throw new IllegalStateException("ClientContext not set");
        }

        String login = requestContext.getLogin();
        AccountLoginEntity accountLoginEntity = null;
        AccountTokenEntity accountTokenEntity = null;

        if (login != null && !login.isEmpty()) {
            accountLoginEntity = accountLoginRepository.findByAll4Fields(tenantCode, authClientId, login, accountId)
                    .orElseThrow(() -> new UsernameNotFoundException("AccountLogin not found with id: " + accountId));
        } else {
            // API key auth: no login, resolve by tenant + client + account
            accountTokenEntity = accountTokenRepository.findByAll4Fields(tenantCode, authClientId, accountId)
                    .orElseThrow(() -> new UsernameNotFoundException("AccountToken not found for account id: " + accountId));
        }
        //requestContext.setAccount(defaultAccountLogin.getAccount().getId().toString());
        requestContext.setAccount(username);
        // Инициализируем lazy-loaded поля внутри транзакции
        //initializeLazyFields(defaultAccountLogin, loginEntity);

        // Получаем роли продуктов для аккаунта
        Set<String> productRoles = getProductRoles(accountId);

        Long actingAccountId = null;

        AccountEntity accountEntity = accountRepository.findByTenantCodeAndId(tenantCode, accountId)
            .orElseThrow(() -> new UsernameNotFoundException("Account not found with id: " + accountId));

        switch (accountEntity.getNodeType()) {
            case SUB: // Для саба данные от родителя
                actingAccountId = accountEntity.getParent() != null
                    ? accountEntity.getParent().getId()
                    : accountEntity.getId();
                break;
            case ACCOUNT:  // Только свой узел 
                actingAccountId = accountEntity.getId();
                break;
            case SYS_ADMIN:
                //ToDO Если сис админ имперсонировался то нужно подменить account
            case TNT_ADMIN:
            case PRODUCT_ADMIN:
                    // От узла - тенант 
                AccountEntity tenantAccount = accountRepository.findByTenantId(accountEntity.getTenant().getId())
                    .orElseThrow(() -> new UsernameNotFoundException("Tenant account not found for tenant: " + tenantCode));
                actingAccountId = tenantAccount.getId();
                break;
             case GROUP_ADMIN:  
                // Родитель - это группа или клиент
                actingAccountId = accountEntity.getParent() != null
                    ? accountEntity.getParent().getId()
                    : accountEntity.getId();
                break;
            case GROUP:
            case CLIENT:
            case TENANT:
                // Это технические узлы. Под ними работать нельзя
                throw new ForbiddenException("Недопустимый тип узла для авторизации");
            default:
                throw new ForbiddenException("Недопустимый тип узла для авторизации");
        }

        // Создаем UserDetails без пароля (JWT авторизация)
        UserDetails userDetails = null;
        if (accountLoginEntity != null) {
            userDetails = UserDetailsImpl.build(accountLoginEntity, productRoles, actingAccountId);
        } 
        if (accountTokenEntity != null) {
            userDetails = UserDetailsImpl.build(accountTokenEntity, accountEntity, productRoles, actingAccountId);
        } 

        return userDetails;
    }

    public UserDetailsImpl impersonateSysAdmin(UserDetailsImpl user, String newTenant) {
        if ("SYS_ADMIN".equals(user.getUserRole())) {
            
            if (newTenant != null && !newTenant.isEmpty()) {
                user.setImpersonatedTenantCode(newTenant); 
                AccountEntity tenantAccount = accountRepository.findTenantAccount(newTenant).orElseThrow(() -> new UsernameNotFoundException("Tenant account not found for tenant: " + newTenant));
                
                user.setActingAccountId(tenantAccount.getId());
                user.setImpersonatedTenantCode(newTenant);
            }
        }
        return user;
    }
    /**
     * Получает все роли продуктов для аккаунта
     */
    private Set<String> getProductRoles(Long accountId) {
        List<Map<String, Object>> roles = productRoleRepository.findAllProductRolesByAccountId(accountId);
        Set<String> allRoles = new HashSet<>();

        for (Map<String, Object> role : roles) {
            String productName = role.get("roleProductCode").toString();
            if (!allRoles.contains(productName)) {
                allRoles.add(productName);
                if (role.get("canRead") != null && (boolean) role.get("canRead")) {
                    allRoles.add(productName + "_READ");
                }
                if (role.get("canQuote") != null && (boolean) role.get("canQuote")) {
                    allRoles.add(productName + "_QUOTE");
                }
                if (role.get("canPolicy") != null && (boolean) role.get("canPolicy")) {
                    allRoles.add(productName + "_POLICY");
                }
                if (role.get("canAddendum") != null && (boolean) role.get("canAddendum")) {
                    allRoles.add(productName + "_ADDENDUM");
                }
                if (role.get("canCancel") != null && (boolean) role.get("canCancel")) {
                    allRoles.add(productName + "_CANCEL");
                }
                if (role.get("canProlongate") != null && (boolean) role.get("canProlongate")) {
                    allRoles.add(productName + "_PROLONGATE");
                }
            }
        }

        return allRoles;
    }
}


package ru.pt.auth.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.model.ClientSecurityConfig;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.security.context.RequestContext;

@Service
public class AccountResolverService {

    private final AccountLoginRepository accountLoginRepository;
    private final ClientService clientService;
    private final LoginRepository loginRepository;

    private final RequestContext requestContext;
    public AccountResolverService(AccountLoginRepository accountLoginRepository, ClientService clientService, LoginRepository loginRepository, RequestContext requestContext) {
        this.clientService = clientService;
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.requestContext = requestContext;
    }

    public void resolveAccounts() {
        // Получаем доступные аккаунты по tenant, client, user
        String tenantCode = requestContext.getTenant();
        String authClientId = requestContext.getClient();
        String userLogin = requestContext.getLogin();
        String accountId = requestContext.getAccount();
        
        if (tenantCode == null || tenantCode.isEmpty()) {
            throw new IllegalStateException("TenantContext not set");
        }
        if (authClientId == null || authClientId.isEmpty()) {
            throw new IllegalStateException("ClientContext not set");
        }
        
        // Client не проверен.
        ClientSecurityConfig clientSecurityConfig = clientService.getConfig(tenantCode, authClientId);
        if (clientSecurityConfig == null) {
            throw new IllegalArgumentException("Client not found: " + tenantCode + " " + authClientId);
        }
        Long defaultAccountId = clientSecurityConfig.defaultAccountId();
    
        if (userLogin == null || userLogin.isEmpty()) {
            // userLogin не задан, значит это клиентский аккаунт.Но нет дефолтного аккаунта.
            if (defaultAccountId == null) {
                throw new IllegalStateException("Default account not set for client: " + authClientId);
            }
            // если accountId задан, то проверяем, что он равен defaultAccountId. Иначе выбрасываем ошибку.
            if (accountId != null && !accountId.equals(defaultAccountId.toString())) {
                throw new IllegalStateException("Account in context is invalid for this user");
            }
            // текущий эккаун это клиентский экаутн.
            requestContext.setAccount(defaultAccountId.toString());
            return;
        }

        // userLogin задан, значит это пользовательский аккаунт.
        // Находим LoginEntity по логину & tenant и пользователь не залочен
        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(tenantCode, userLogin)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + userLogin));
    
        // Находим все AccountLoginEntity для этого пользователя
        List<AccountLoginEntity> accountLogins = accountLoginRepository.findByTenantCodeAndClientIdAndUserLogin(
                tenantCode, 
                authClientId, 
                userLogin);
    
        if (accountLogins.isEmpty()) {
            throw new UsernameNotFoundException("No accounts found for user: " + userLogin);
        }
    
        // Берем дефолтный аккаунт или первый доступный
        AccountLoginEntity defaultAccountLogin = accountLogins.stream()
                    .filter(al -> Boolean.TRUE.equals(al.getDefault()))
                    .findFirst()
                    .orElse(accountLogins.getFirst());
        
        requestContext.setAccount(defaultAccountLogin.getAccount().getId().toString());
    }
}

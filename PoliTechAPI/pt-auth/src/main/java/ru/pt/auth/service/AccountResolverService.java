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
import ru.pt.api.dto.auth.ClientAuthType;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
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
            throw new NotFoundException("TenantContext not set");
        }
        if (authClientId == null || authClientId.isEmpty()) {
            throw new NotFoundException("ClientContext not set");
        }
        
        // Client не проверен.
        ClientSecurityConfig clientSecurityConfig = clientService.getConfig(tenantCode, authClientId);
        if (clientSecurityConfig == null) {
            throw new NotFoundException("Client not found: " + tenantCode + " " + authClientId);
        }

        // Если это CLIENT_AUTH то всегда берем экаутн с клиента
        if (clientSecurityConfig.authLevel() == ClientAuthType.CLIENT) {
            //  это клиентский аккаунт.Но нет дефолтного аккаунта.
            Long defaultAccountId = clientSecurityConfig.defaultAccountId();

            if (defaultAccountId == null) {
                throw new UnprocessableEntityException("Default account not set for client: " + authClientId);
            }
            // если accountId задан, то проверяем, что он равен defaultAccountId. Иначе выбрасываем ошибку.
            if (accountId != null && !accountId.equals(defaultAccountId.toString())) {
                throw new UnprocessableEntityException("Account in context is invalid for this user");
            }
            // текущий эккаун это клиентский экаутн.
            requestContext.setAccount(defaultAccountId.toString());
            return;
        }

        // Это USER_AUTH и если экаунт передан в заголовке то ничего не переопределяем. Проверим в следующем фильтре
        if (accountId != null && !accountId.isEmpty()) {
            return;
        }

        // uэто пользовательский аккаунт.
        // Находим LoginEntity по логину & tenant и пользователь не залочен
        LoginEntity loginEntity = loginRepository.findByTenantCodeAndUserLogin(tenantCode, userLogin)
                .orElseThrow(() -> new NotFoundException("User not found with login: " + userLogin));
    
        // Находим все AccountLoginEntity для этого пользователя
        List<AccountLoginEntity> accountLogins = accountLoginRepository.findByTenantCodeAndClientIdAndUserLogin(
                tenantCode, 
                authClientId, 
                userLogin);
    
        if (accountLogins.isEmpty()) {
            throw new NotFoundException("No accounts found for user: " + userLogin);
        }
    
        // Берем дефолтный аккаунт или первый доступный
        AccountLoginEntity defaultAccountLogin = accountLogins.stream()
                    .filter(al -> Boolean.TRUE.equals(al.getDefault()))
                    .findFirst()
                    .orElse(accountLogins.getFirst());
        
        requestContext.setAccount(defaultAccountLogin.getAccount().getId().toString());
    }
}

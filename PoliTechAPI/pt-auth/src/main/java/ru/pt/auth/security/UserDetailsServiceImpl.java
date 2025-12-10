package ru.pt.auth.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.repository.ProductRoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация UserDetailsService для Spring Security.
 * Загружает пользователя по логину из базы данных.
 */
@Service
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final ProductRoleRepository productRoleRepository;

    public UserDetailsServiceImpl(LoginRepository loginRepository,
                                  AccountLoginRepository accountLoginRepository,
                                  ProductRoleRepository productRoleRepository) {
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.productRoleRepository = productRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Находим LoginEntity по логину
        LoginEntity loginEntity = loginRepository.findByUserLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));

        // Находим все AccountLoginEntity для этого пользователя
        List<AccountLoginEntity> accountLogins = accountLoginRepository.findByUserLogin(username);

        if (accountLogins.isEmpty()) {
            throw new UsernameNotFoundException("No accounts found for user: " + username);
        }

        // Берем дефолтный аккаунт или первый доступный
        AccountLoginEntity defaultAccountLogin = accountLogins.stream()
                .filter(AccountLoginEntity::getDefault)
                .findFirst()
                .orElse(accountLogins.getFirst());

        // Инициализируем lazy-loaded поля внутри транзакции
        initializeLazyFields(defaultAccountLogin, loginEntity);

        // Получаем роли продуктов для аккаунта
        Set<String> productRoles = getProductRoles(defaultAccountLogin.getAccount().getId());

        // Создаем UserDetails без пароля (JWT авторизация)
        return UserDetailsImpl.build(loginEntity, defaultAccountLogin, productRoles);
    }

    /**
     * Загружает пользователя по логину и ID аккаунта
     */
    public UserDetails loadUserByUsernameAndAccountId(String username, Long accountId) throws UsernameNotFoundException {
        LoginEntity loginEntity = loginRepository.findByUserLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));

        AccountLoginEntity accountLogin = accountLoginRepository.findByUserLoginAndAccountId(username, accountId)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found for user: " + username + " and accountId: " + accountId));

        // Инициализируем lazy-loaded поля
        initializeLazyFields(accountLogin, loginEntity);

        Set<String> productRoles = getProductRoles(accountLogin.getAccount().getId());

        return UserDetailsImpl.build(loginEntity, accountLogin, productRoles);
    }

    /**
     * Загружает пользователя по логину и клиенту
     */
    public UserDetails loadUserByUsernameAndClient(String username, String client) throws UsernameNotFoundException {
        LoginEntity loginEntity = loginRepository.findByUserLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with login: " + username));

        List<AccountLoginEntity> accountLogins = accountLoginRepository.findByClientAndLogin(client, username);

        if (accountLogins.isEmpty()) {
            throw new UsernameNotFoundException("No accounts found for user: " + username + " and client: " + client);
        }

        AccountLoginEntity defaultAccountLogin = accountLogins.stream()
                .filter(AccountLoginEntity::getDefault)
                .findFirst()
                .orElse(accountLogins.get(0));

        // Инициализируем lazy-loaded поля
        initializeLazyFields(defaultAccountLogin, loginEntity);

        Set<String> productRoles = getProductRoles(defaultAccountLogin.getAccount().getId());

        return UserDetailsImpl.build(loginEntity, defaultAccountLogin, productRoles);
    }

    /**
     * Инициализирует lazy-loaded поля для избежания LazyInitializationException
     */
    private void initializeLazyFields(AccountLoginEntity accountLogin, LoginEntity loginEntity) {
        // Инициализируем связанные сущности, обращаясь к их методам
        if (accountLogin.getAccount() != null) {
            accountLogin.getAccount().getId(); // Trigger lazy loading
            accountLogin.getAccount().getName(); // Trigger lazy loading
        }
        if (accountLogin.getClient() != null) {
            accountLogin.getClient().getId();
            accountLogin.getClient().getName();
        }
        if (loginEntity.getTenant() != null) {
            loginEntity.getTenant().getId();
            loginEntity.getTenant().getName();
        }
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


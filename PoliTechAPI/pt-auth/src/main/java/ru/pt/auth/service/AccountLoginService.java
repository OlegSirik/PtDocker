package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.repository.AccountLoginRepository;

import java.util.Optional;

/**
 * Сервис для работы с привязками пользователей к аккаунтам (таблица acc_account_logins)
 */
@Service
public class AccountLoginService {

    private final AccountLoginRepository accountLoginRepository;

    public AccountLoginService(AccountLoginRepository accountLoginRepository) {
        this.accountLoginRepository = accountLoginRepository;
    }

    /**
     * Проверка существования и получение связи между user_login и client_id
     * с проверкой что запись в acc_logins не удалена
     */
    public Optional<AccountLoginEntity> validateUserLoginAndClientId(String userLogin, String clientId, String tenantCode) {
        return accountLoginRepository.findByUserLoginAndClientIdWithValidation(userLogin, clientId, tenantCode);
    }

    /**
     * Проверка существования user_login в БД acc_logins с привязкой к тенанту
     * и проверкой что запись не удалена
     */
    public boolean validateUserLoginInTenant(String userLogin, String tenantCode) {
        return accountLoginRepository.findByUserLoginAndTenantCode(userLogin, tenantCode).isPresent();
    }
}


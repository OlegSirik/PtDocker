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
    public Optional<AccountLoginEntity> validateUserLoginAndClientId(String userLogin, String clientId) {
        return accountLoginRepository.findByUserLoginAndClientIdWithValidation(userLogin, clientId);
    }
}


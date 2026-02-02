package ru.pt.api.service.auth;

import ru.pt.api.dto.auth.AccountLogin;

import java.util.List;

/**
 * Сервис для работы с привязками пользователей к аккаунтам (таблица acc_account_logins)
 */
public interface AccountLoginService {

    /**
     * Создать привязку логина к аккаунту
     * Если логин не существует в acc_logins, создаёт его
     * @param accountId идентификатор аккаунта
     * @param login данные логина
     * @return созданная привязка
     */
    AccountLogin createLogin(Long accountId, AccountLogin login);

    /**
     * Получить все привязки логинов для аккаунта
     * @param accountId идентификатор аккаунта
     * @return список привязок
     */
    List<AccountLogin> getLoginsByAccountId(Long accountId);

    /**
     * Удалить привязку логина к аккаунту (только из acc_account_logins)
     * @param accountId идентификатор аккаунта
     * @param userLogin логин пользователя
     */
    void deleteLogin(Long accountId, String userLogin);

    /**
     * Проверить существование привязки логина к аккаунту
     * @param accountId идентификатор аккаунта
     * @param userLogin логин пользователя
     * @return true, если привязка существует
     */
    boolean loginExists(Long accountId, String userLogin);
}

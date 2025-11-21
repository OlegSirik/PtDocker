package ru.pt.api.service.auth;

import ru.pt.api.dto.auth.AccountToken;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с токенами аккаунтов
 */
public interface AccountTokenService {

    /**
     * Создать новый токен для пользователя
     *
     * @param userLogin логин пользователя
     * @param clientId идентификатор клиента
     * @param token строка токена
     * @return созданный токен
     */
    AccountToken createToken(String userLogin, Long clientId, String token);

    /**
     * Обновить существующий токен
     *
     * @param userLogin логин пользователя
     * @param clientId идентификатор клиента
     * @param newToken новая строка токена
     * @return обновленный токен
     */
    AccountToken updateToken(String userLogin, Long clientId, String newToken);

    /**
     * Получить токен по userLogin и clientId
     *
     * @param userLogin логин пользователя
     * @param clientId идентификатор клиента
     * @return токен, если найден
     */
    Optional<AccountToken> getToken(String userLogin, Long clientId);

    /**
     * Получить все токены для пользователя
     *
     * @param userLogin логин пользователя
     * @return список токенов
     */
    List<AccountToken> getTokensByUserLogin(String userLogin);

    /**
     * Удалить токен
     *
     * @param userLogin логин пользователя
     * @param clientId идентификатор клиента
     */
    void deleteToken(String userLogin, Long clientId);

    /**
     * Проверить существование токена
     *
     * @param token строка токена
     * @param clientId идентификатор клиента
     * @return true, если токен существует
     */
    boolean tokenExists(String token, Long clientId);
}


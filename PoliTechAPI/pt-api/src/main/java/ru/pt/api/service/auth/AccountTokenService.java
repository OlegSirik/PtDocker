package ru.pt.api.service.auth;

import ru.pt.api.dto.auth.AccountToken;

import java.util.List;

/**
 * Сервис для работы с токенами аккаунтов
 * Токен в данном контексте это анонимный ключ доступа. 
 * Позволяет группировать продажи на спец счета с разбивкой по скидочному коду. ( например ) 
 */
public interface AccountTokenService {

    /**
     * Создать новый токен для клиента
     * @param clientId идентификатор клиента (accountId)
     * @param token строка токена
     * @return созданный токен
     */
    AccountToken createToken(Long accountId, String token);

    /**
     * Получить все токены для клиента
     * @param clientId идентификатор клиента
     * @return список токенов
     */
    List<AccountToken> getTokens(Long accountId);

    /**
     * Удалить токен
     * @param clientId идентификатор клиента
     * @param token строка токена
     */
    void deleteToken(Long accountId, String token);

    /**
     * Проверить существование токена
     * @param clientId идентификатор клиента
     * @param token строка токена
     * @return true, если токен существует
     */
    boolean tokenExists(Long clientId, String token);
}

package ru.pt.auth.identity;

import java.util.Map;
import ru.pt.auth.model.AuthType;

/**
 * Получение JWT по логину/паролю у внешнего IdP (password grant).
 */
public interface ExternalJwtAuthenticator {

    /**
     * Поддерживает ли данный аутентификатор указанный тип аутентификации и конфигурацию.
     */
    boolean supports(AuthType authType, Map<String, String> config);

    /**
     * @param tenantCode код тенанта (tid)
     * @param clientId client_id приложения
     * @param login логин пользователя
     * @param password пароль пользователя
     * @param config per-tenant auth_config (Map<String,String>)
     * @return access_token (JWT) или выбрасывает исключение при ошибке
     */
    String authenticate(String tenantCode,
                        String clientId,
                        String login,
                        String password,
                        Map<String, String> config);
}


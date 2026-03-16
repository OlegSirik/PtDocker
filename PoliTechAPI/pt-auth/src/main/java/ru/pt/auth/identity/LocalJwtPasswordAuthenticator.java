package ru.pt.auth.identity;

import org.springframework.stereotype.Component;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.service.SimpleAuthService;

import java.util.Map;

/**
 * Локальная аутентификация по login/password с выдачей внутреннего JWT.
 */
@Component
public class LocalJwtPasswordAuthenticator implements ExternalJwtAuthenticator {

    private final SimpleAuthService simpleAuthService;

    public LocalJwtPasswordAuthenticator(SimpleAuthService simpleAuthService) {
        this.simpleAuthService = simpleAuthService;
    }

    @Override
    public boolean supports(AuthType authType, Map<String, String> config) {
        return authType == AuthType.LOCAL_JWT;
    }

    @Override
    public String authenticate(String tenantCode,
                               String clientId,
                               String login,
                               String password,
                               Map<String, String> config) {
        return simpleAuthService.authenticate(tenantCode, login, password, clientId);
    }
}


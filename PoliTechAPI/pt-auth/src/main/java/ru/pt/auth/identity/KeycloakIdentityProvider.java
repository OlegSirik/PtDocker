package ru.pt.auth.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.service.keycloak.KeycloakService;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.model.AuthProperties;
import ru.pt.auth.model.AuthType;

import java.util.List;
import java.util.Map;

/**
 * IdentityProvider для интеграции с Keycloak.
 * Используется, когда у тенанта authType = KEYCLOAK.
 */
@Component
public class KeycloakIdentityProvider implements IdentityProvider {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakIdentityProvider.class);

    private final KeycloakService keycloakService;

    public KeycloakIdentityProvider(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @Override
    public AuthType supportedAuthType() {
        return AuthType.KEYCLOAK;
    }

    @Override
    public void createClient(TenantEntity tenant, ClientEntity client) {
        Map<String, String> cfg = tenant.getAuthConfig();
        if (cfg == null || cfg.isEmpty()) {
            return;
        }

        boolean hasAdmin =
                cfg.containsKey(AuthProperties.ADMIN_CLIENT_ID.value()) &&
                cfg.containsKey(AuthProperties.ADMIN_CLIENT_SECRET.value());
        if (!hasAdmin) {
            return;
        }

        try {
            keycloakService.createConfidentialClient(
                    client.getClientId(),
                    client.getName(),
                    List.of("*")
            );
        } catch (Exception e) {
            logger.warn("Failed to create client {} in Keycloak for tenant {}: {}", client.getClientId(), tenant.getCode(), e.getMessage());
            throw new InternalServerErrorException("Failed to create client in external IdP", e);
        }
    }

    @Override
    public void createUser(TenantEntity tenant, LoginEntity login) {
        Map<String, String> cfg = tenant.getAuthConfig();
        if (cfg == null || cfg.isEmpty()) {
            return;
        }

        boolean hasAdmin =
                cfg.containsKey(AuthProperties.ADMIN_CLIENT_ID.value()) &&
                cfg.containsKey(AuthProperties.ADMIN_CLIENT_SECRET.value());
        if (!hasAdmin) {
            return;
        }

        try {
            // Email и доп. данные можно будет добавить позже, сейчас используем только логин.
            keycloakService.createUserWithPassword(login.getUserLogin(), null, null, false);
        } catch (Exception e) {
            logger.warn("Failed to create user {} in Keycloak for tenant {}: {}", login.getUserLogin(), tenant.getCode(), e.getMessage());
            throw new InternalServerErrorException("Failed to create user in external IdP", e);
        }
    }
}


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
import java.util.Optional;

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

        if (!hasKeycloakAdminCredentials(cfg)) {
            return;
        }

        try {
            if (client.getAuthLevel().equalsIgnoreCase("CLIENT")) {
                keycloakService.createConfidentialClient(cfg, client.getClientId(), client.getName(), List.of("*"));
            } else {
                keycloakService.createPublicClient(cfg, client.getClientId(), client.getName(), List.of("*"));
            }
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

        if (!hasKeycloakAdminCredentials(cfg)) {
            return;
        }

        try {
            // Email и доп. данные можно будет добавить позже, сейчас используем только логин.
            keycloakService.createUserWithPassword(cfg, login.getUserLogin(), null, login.getPassword(), false);
        } catch (Exception e) {
            logger.warn("Failed to create user {} in Keycloak for tenant {}: {}", login.getUserLogin(), tenant.getCode(), e.getMessage());
            throw new InternalServerErrorException("Failed to create user in external IdP", e);
        }
    }

    @Override
    public void setUserPassword(TenantEntity tenant, LoginEntity login, String newPassword, boolean temporary) {
        Map<String, String> cfg = tenant.getAuthConfig();
        if (cfg == null || cfg.isEmpty()) {
            return;
        }
        if (!hasKeycloakAdminCredentials(cfg)) {
            return;
        }
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }
        String username = login.getUserLogin();
        try {
            Optional<String> userId = keycloakService.findUserIdByUsername(cfg, username);
            if (userId.isEmpty()) {
                logger.warn("Keycloak user not found for login '{}' in tenant {}", username, tenant.getCode());
                throw new InternalServerErrorException("Keycloak user not found for login: " + username);
            }
            keycloakService.updateUserPassword(cfg, userId.get(), newPassword, temporary);
        } catch (InternalServerErrorException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Failed to set password in Keycloak for {} (tenant {}): {}", username, tenant.getCode(), e.getMessage());
            throw new InternalServerErrorException("Failed to set user password in external IdP", e);
        }
    }

    /** Password grant (adminUsername+adminPassword) или client_credentials (adminClientId+adminClientSecret). */
    private static boolean hasKeycloakAdminCredentials(Map<String, String> cfg) {
        boolean password = cfg.containsKey(AuthProperties.ADMIN_USERNAME.value())
                && cfg.containsKey(AuthProperties.ADMIN_PASSWORD.value())
                && cfg.get(AuthProperties.ADMIN_USERNAME.value()) != null
                && !cfg.get(AuthProperties.ADMIN_USERNAME.value()).isBlank()
                && cfg.get(AuthProperties.ADMIN_PASSWORD.value()) != null
                && !cfg.get(AuthProperties.ADMIN_PASSWORD.value()).isBlank();
        boolean clientCreds = cfg.containsKey(AuthProperties.ADMIN_CLIENT_ID.value())
                && cfg.containsKey(AuthProperties.ADMIN_CLIENT_SECRET.value());
        return password || clientCreds;
    }
}


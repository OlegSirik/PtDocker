package ru.pt.auth.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.pt.auth.model.AuthProperties;
import ru.pt.auth.model.AuthType;

import java.util.Map;

/**
 * Реализация ExternalJwtAuthenticator для Keycloak по password grant.
 * Ожидает в tenant.authConfig:
 * - issuer (AuthProperties.ISSUER)        -> базовый issuer, используется для построения token endpoint
 * - adminClientId/adminClientSecret       -> client_id / client_secret для password grant
 */
@Component
public class KeycloakPasswordGrantAuthenticator implements ExternalJwtAuthenticator {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakPasswordGrantAuthenticator.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean supports(AuthType authType, Map<String, String> config) {
        return (authType == AuthType.KEYCLOAK || authType == AuthType.JWT)
                && config != null
                && config.containsKey(AuthProperties.ISSUER.value());
    }

    @Override
    public String authenticate(String tenantCode,
                               String clientId,
                               String login,
                               String password,
                               Map<String, String> config) {
        if (config == null || config.isEmpty()) {
            throw new IllegalStateException("auth_config is empty for tenant " + tenantCode);
        }

        String issuer = config.get(AuthProperties.ISSUER.value());
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("Missing issuer in auth_config for tenant " + tenantCode);
        }

        // Стандартный endpoint: {issuer}/protocol/openid-connect/token
        String tokenUrl = issuer.endsWith("/")
                ? issuer + "protocol/openid-connect/token"
                : issuer + "/protocol/openid-connect/token";

        //String passwordClientId = config.get(AuthProperties.ADMIN_CLIENT_ID.value());
        //String passwordClientSecret = config.get(AuthProperties.ADMIN_CLIENT_SECRET.value());

        //if (passwordClientId == null || passwordClientSecret == null) {
        //    throw new IllegalStateException("Missing client credentials in auth_config for tenant " + tenantCode);
        //}

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("username", login);
        body.add("password", password);
        
        //body.add("client_id", "admin-cli");
        body.add("client_id", clientId);
        //body.add("client_id", passwordClientId);
        //body.add("client_secret", passwordClientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Keycloak token endpoint returned status " + response.getStatusCode());
            }
            Object token = response.getBody().get("access_token");
            if (token == null) {
                throw new IllegalStateException("Keycloak response does not contain access_token");
            }
            return token.toString();
        } catch (Exception e) {
            logger.warn("Failed to obtain JWT from Keycloak for tenant {} and user {}: {}", tenantCode, login, e.getMessage());
            throw new IllegalStateException("Failed to obtain JWT from external IdP", e);
        }
    }
}


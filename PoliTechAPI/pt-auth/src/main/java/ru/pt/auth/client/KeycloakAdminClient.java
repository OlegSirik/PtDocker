package ru.pt.auth.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.auth.model.AuthProperties;
import ru.pt.auth.model.keycloak.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Low-level REST client for Keycloak Admin API
 */
@Component
public class KeycloakAdminClient {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminClient.class);

    private final RestTemplate restTemplate;

    public KeycloakAdminClient(@Qualifier("keycloakRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Параметры для POST .../realms/{tokenRealm}/protocol/openid-connect/token
     * Либо password grant (как curl: admin-cli + username/password), либо client_credentials.
     */
    private record AdminContext(
            String serverUrl,
            String tokenRealm,
            boolean passwordGrant,
            String tokenClientId,
            String username,
            String password,
            String clientSecret
    ) {}

    private AdminContext adminContext(Map<String, String> authConfig) {
        if (authConfig == null || authConfig.isEmpty()) {
            throw new IllegalStateException("auth_config is empty for Keycloak admin");
        }

        String issuer = authConfig.get(AuthProperties.ISSUER.value());
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("Missing issuer in auth_config for Keycloak admin");
        }

        String tokenRealm = authConfig.get(AuthProperties.ADMIN_REALM.value());
        if (tokenRealm == null || tokenRealm.isBlank()) {
            tokenRealm = extractRealmFromIssuer(issuer);
        }

        String serverUrl = extractServerUrlFromIssuer(issuer);

        String adminUsername = authConfig.get(AuthProperties.ADMIN_USERNAME.value());
        String adminPassword = authConfig.get(AuthProperties.ADMIN_PASSWORD.value());
        boolean hasPasswordGrant = adminUsername != null && !adminUsername.isBlank()
                && adminPassword != null && !adminPassword.isBlank();

        if (hasPasswordGrant) {
            String tokenClientId = authConfig.get(AuthProperties.ADMIN_TOKEN_CLIENT_ID.value());
            if (tokenClientId == null || tokenClientId.isBlank()) {
                tokenClientId = "admin-cli";
            }
            return new AdminContext(serverUrl, tokenRealm, true, tokenClientId, adminUsername, adminPassword, null);
        }

        String adminClientId = authConfig.get(AuthProperties.ADMIN_CLIENT_ID.value());
        String adminClientSecret = authConfig.get(AuthProperties.ADMIN_CLIENT_SECRET.value());
        if (adminClientId == null || adminClientId.isBlank()
                || adminClientSecret == null || adminClientSecret.isBlank()) {
            throw new IllegalStateException(
                    "Keycloak admin: укажите либо adminUsername+adminPassword (password grant), "
                            + "либо adminClientId+adminClientSecret (client_credentials) в auth_config");
        }
        return new AdminContext(serverUrl, tokenRealm, false, adminClientId, null, null, adminClientSecret);
    }

    private static String extractServerUrlFromIssuer(String issuer) {
        String normalized = issuer.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String marker = "/realms/";
        int idx = normalized.indexOf(marker);
        if (idx < 0) {
            throw new IllegalStateException("Cannot extract serverUrl from issuer: " + issuer);
        }
        return normalized.substring(0, idx);
    }

    private static String extractRealmFromIssuer(String issuer) {
        String normalized = issuer.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        String marker = "/realms/";
        int idx = normalized.indexOf(marker);
        if (idx < 0) {
            throw new IllegalStateException("Cannot extract realm from issuer: " + issuer);
        }
        String after = normalized.substring(idx + marker.length());
        int slash = after.indexOf('/');
        return slash >= 0 ? after.substring(0, slash) : after;
    }
    
    /**
     * Get admin access token
     */
    private String getAdminToken(Map<String, String> authConfig) {
        AdminContext ctx = adminContext(authConfig);
        String url = ctx.serverUrl() + "/realms/" + ctx.tokenRealm() + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (ctx.passwordGrant()) {
            // как: curl .../realms/master/protocol/openid-connect/token
            //   -d grant_type=password -d username=... -d password=... -d client_id=admin-cli
            body.add("grant_type", "password");
            body.add("client_id", ctx.tokenClientId());
            body.add("username", ctx.username());
            body.add("password", ctx.password());
        } else {
            body.add("grant_type", "client_credentials");
            body.add("client_id", ctx.tokenClientId());
            body.add("client_secret", ctx.clientSecret());
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<KeycloakTokenResponse> response = restTemplate.postForEntity(
                    url, request, KeycloakTokenResponse.class);
            
            if (response.getBody() == null) {
                throw new InternalServerErrorException("Failed to get admin token: empty response");
            }
            
            logger.debug("Successfully obtained admin token");
            return response.getBody().getAccessToken();
        } catch (Exception e) {
            logger.error("Failed to get admin token: {}", e.getMessage(), e);
            throw new InternalServerErrorException("Failed to authenticate with Keycloak", e);
        }
    }
    
    /**
     * Create HTTP headers with Bearer token
     */
    private HttpHeaders createAuthHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }
    
    /**
     * Create a new client
     */
    public String createClient(String realm, Map<String, String> authConfig, KeycloakClientRequest clientRequest) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/clients";
        
        HttpEntity<KeycloakClientRequest> request = new HttpEntity<>(clientRequest, createAuthHeaders(token));
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Void.class);
            
            // Extract client ID from Location header
            String location = response.getHeaders().getFirst("Location");
            if (location == null) {
                throw new InternalServerErrorException("Client created but no Location header returned");
            }
            
            String clientId = location.substring(location.lastIndexOf('/') + 1);
            logger.info("Created Keycloak client: {}", clientId);
            return clientId;
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to create client: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalServerErrorException("Failed to create Keycloak client", e);
        }
    }
    
    /**
     * Get client secret
     */
    public String getClientSecret(String realm, Map<String, String> authConfig, String clientId) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/clients/" + clientId + "/client-secret";
        
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, Map.class);
            
            if (response.getBody() == null || !response.getBody().containsKey("value")) {
                throw new InternalServerErrorException("Failed to get client secret");
            }
            
            return (String) response.getBody().get("value");
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to get client secret: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to get client secret", e);
        }
    }
    
    /**
     * Regenerate client secret
     */
    public String regenerateClientSecret(String realm, Map<String, String> authConfig, String clientId) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/clients/" + clientId + "/client-secret";
        
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Map.class);
            
            if (response.getBody() == null || !response.getBody().containsKey("value")) {
                throw new InternalServerErrorException("Failed to regenerate client secret");
            }
            
            logger.info("Regenerated secret for client: {}", clientId);
            return (String) response.getBody().get("value");
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to regenerate client secret: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to regenerate client secret", e);
        }
    }
    
    /**
     * Create a new user
     */
    public String createUser(String realm, Map<String, String> authConfig, KeycloakUserRequest userRequest) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users";
        
        HttpEntity<KeycloakUserRequest> request = new HttpEntity<>(userRequest, createAuthHeaders(token));
        
        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, Void.class);
            
            String location = response.getHeaders().getFirst("Location");
            if (location == null) {
                throw new InternalServerErrorException("User created but no Location header returned");
            }
            
            String userId = location.substring(location.lastIndexOf('/') + 1);
            logger.info("Created Keycloak user: {}", userId);
            return userId;
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to create user: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalServerErrorException("Failed to create Keycloak user", e);
        }
    }
    
    /**
     * Найти пользователя по username (exact=true), вернуть первый id или пусто.
     */
    public Optional<String> findUserIdByUsername(String realm, Map<String, String> authConfig, String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = UriComponentsBuilder
                .fromHttpUrl(ctx.serverUrl() + "/admin/realms/" + realm + "/users")
                .queryParam("username", username)
                .queryParam("exact", true)
                .encode()
                .toUriString();

        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, JsonNode.class);
            JsonNode body = response.getBody();
            if (body == null || !body.isArray() || body.isEmpty()) {
                return Optional.empty();
            }
            JsonNode first = body.get(0);
            if (first != null && first.hasNonNull("id")) {
                return Optional.of(first.get("id").asText());
            }
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            logger.error("Failed to search user by username: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalServerErrorException("Failed to find user in Keycloak by username", e);
        }
    }

    /**
     * Get user by ID
     */
    public JsonNode getUser(String realm, Map<String, String> authConfig, String userId) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users/" + userId;
        
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, JsonNode.class);
            
            return response.getBody();
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to get user: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to get user from Keycloak", e);
        }
    }
    
    /**
     * Send email verification
     */
    public void sendVerifyEmail(String realm, Map<String, String> authConfig, String userId) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users/" + userId + "/send-verify-email";
        
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            logger.info("Sent verification email to user: {}", userId);
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to send verification email: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to send verification email", e);
        }
    }
    
    /**
     * Execute required actions email
     */
    public void executeActionsEmail(String realm, Map<String, String> authConfig, String userId, List<String> actions) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users/" + userId + "/execute-actions-email";
        
        HttpEntity<List<String>> request = new HttpEntity<>(actions, createAuthHeaders(token));
        
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            logger.info("Sent action email to user: {} with actions: {}", userId, actions);
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to send action email: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to send action email", e);
        }
    }
    
    /**
     * Update user
     */
    public void updateUser(String realm, Map<String, String> authConfig, String userId, KeycloakUserRequest userRequest) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users/" + userId;
        
        HttpEntity<KeycloakUserRequest> request = new HttpEntity<>(userRequest, createAuthHeaders(token));
        
        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            logger.info("Updated user: {}", userId);
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to update user: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to update user", e);
        }
    }

    /**
     * Установить / сбросить пароль пользователя (Keycloak Admin API: PUT .../users/{id}/reset-password).
     */
    public void resetUserPassword(String realm, Map<String, String> authConfig, String userId, String password, boolean temporary) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";

        KeycloakCredential credential = KeycloakCredential.password(password, temporary);
        HttpEntity<KeycloakCredential> request = new HttpEntity<>(credential, createAuthHeaders(token));

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            logger.info("Reset password for Keycloak user: {}", userId);
        } catch (HttpClientErrorException e) {
            logger.error("Failed to reset user password: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new InternalServerErrorException("Failed to reset user password in Keycloak", e);
        }
    }
    
    /**
     * Delete client
     */
    public void deleteClient(String realm, Map<String, String> authConfig, String clientId) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/clients/" + clientId;
        
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            logger.info("Deleted client: {}", clientId);
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to delete client: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to delete client", e);
        }
    }
    
    /**
     * Delete user
     */
    public void deleteUser(String realm, Map<String, String> authConfig, String userId) {
        AdminContext ctx = adminContext(authConfig);
        String token = getAdminToken(authConfig);
        String url = ctx.serverUrl() + "/admin/realms/" + realm + "/users/" + userId;
        
        HttpEntity<Void> request = new HttpEntity<>(createAuthHeaders(token));
        
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);
            logger.info("Deleted user: {}", userId);
            
        } catch (HttpClientErrorException e) {
            logger.error("Failed to delete user: {}", e.getMessage());
            throw new InternalServerErrorException("Failed to delete user", e);
        }
    }
}

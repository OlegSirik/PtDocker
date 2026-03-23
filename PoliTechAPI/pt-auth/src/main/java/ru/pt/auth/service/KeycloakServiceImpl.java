package ru.pt.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.pt.api.dto.keycloak.KeycloakClientResponse;
import ru.pt.api.dto.keycloak.KeycloakUserResponse;
import ru.pt.api.service.keycloak.KeycloakService;
import ru.pt.auth.client.KeycloakAdminClient;
import ru.pt.auth.model.keycloak.KeycloakClientRequest;
import ru.pt.auth.model.keycloak.KeycloakUserRequest;
import ru.pt.auth.model.AuthProperties;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakServiceImpl.class);
    
    private final KeycloakAdminClient keycloakClient;
    
    @Override
    public KeycloakClientResponse createConfidentialClient(Map<String, String> authConfig, String clientId, String clientName, List<String> redirectUris) {
        logger.info("Creating confidential client in Keycloak: {}", clientId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        // Build client request
        KeycloakClientRequest clientRequest = KeycloakClientRequest.builder()
                .clientId(clientId)
                .name(clientName)
                .enabled(true)
                .publicClient(false)  // Confidential client
                .serviceAccountsEnabled(true)  // Enable service account
                .standardFlowEnabled(true)  // Enable OAuth2 Authorization Code flow
                .directAccessGrantsEnabled(true)  // Enable Resource Owner Password Credentials flow
                .implicitFlowEnabled(false)  // Disable implicit flow (not recommended)
                .redirectUris(redirectUris != null ? redirectUris : List.of("*"))
                .webOrigins(List.of("*"))  // Allow all origins (configure properly in production)
                .protocol("openid-connect")
                .build();
        
        // Create client
        String keycloakClientId = keycloakClient.createClient(targetRealm, authConfig, clientRequest);
        
        // Get client secret
        String secret = keycloakClient.getClientSecret(targetRealm, authConfig, keycloakClientId);
        
        logger.info("Created confidential client: {} with ID: {}", clientId, keycloakClientId);
        
        return new KeycloakClientResponse(keycloakClientId, clientId, secret, clientName);
    }

    @Override
    public KeycloakClientResponse createPublicClient(Map<String, String> authConfig, String clientId, String clientName, List<String> redirectUris) {
        logger.info("Creating public client in Keycloak: {}", clientId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        // Build client request
        KeycloakClientRequest clientRequest = KeycloakClientRequest.builder()
                .clientId(clientId)
                .name(clientName)
                .enabled(true)
                .publicClient(true)  // Public client
                .serviceAccountsEnabled(false)  // Disable service account
                .standardFlowEnabled(true)  // Enable OAuth2 Authorization Code flow
                .directAccessGrantsEnabled(true)  // Enable Resource Owner Password Credentials flow
                .implicitFlowEnabled(false)  // Disable implicit flow (not recommended)
                .redirectUris(redirectUris != null ? redirectUris : List.of("*"))
                .webOrigins(List.of("*"))  // Allow all origins (configure properly in production)
                .protocol("openid-connect")
                .build();
        
        // Create client
        String keycloakClientId = keycloakClient.createClient(targetRealm, authConfig, clientRequest);
        
        logger.info("Created public client: {} with ID: {}", clientId, keycloakClientId);
        
        return new KeycloakClientResponse(keycloakClientId, clientId, null, clientName);
    }
    
    @Override
    public String regenerateClientSecret(Map<String, String> authConfig, String keycloakClientId) {
        logger.info("Regenerating secret for client: {}", keycloakClientId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        String newSecret = keycloakClient.regenerateClientSecret(targetRealm, authConfig, keycloakClientId);
        
        logger.info("Successfully regenerated secret for client: {}", keycloakClientId);
        return newSecret;
    }
    
    @Override
    public KeycloakUserResponse createUserWithPassword(Map<String, String> authConfig, String username, String email, String password, boolean emailVerified) {
        logger.info("Creating user with password: {}", username);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        // Build user request (пароль задаём отдельным reset-password после создания)
        KeycloakUserRequest userRequest = KeycloakUserRequest.builder()
                .username(username)
                .email(email)
                .enabled(true)
                .emailVerified(emailVerified)
                .build();
        
        // Create user
        String userId = keycloakClient.createUser(targetRealm, authConfig, userRequest);

        if (password != null && !password.isBlank()) {
            keycloakClient.resetUserPassword(targetRealm, authConfig, userId, password, false);
            logger.info("Set initial password for Keycloak user: {} ({})", username, userId);
        }
        
        // Send verification email if not already verified
        if (!emailVerified) {
            try {
                keycloakClient.sendVerifyEmail(targetRealm, authConfig, userId);
                logger.info("Sent verification email to user: {}", username);
            } catch (Exception e) {
                logger.warn("Failed to send verification email to user {}: {}", username, e.getMessage());
            }
        }
        
        logger.info("Created user with password: {} (ID: {})", username, userId);
        
        return new KeycloakUserResponse(userId, username, email, emailVerified);
    }
    
    @Override
    public KeycloakUserResponse createUserWithEmailOtp(Map<String, String> authConfig, String username, String email) {
        logger.info("Creating user with email OTP: {}", username);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        // Build user request with required actions
        KeycloakUserRequest userRequest = KeycloakUserRequest.builder()
                .username(username)
                .email(email)
                .enabled(true)
                .emailVerified(false)
                .requiredActions(List.of(
                        "VERIFY_EMAIL",      // User must verify email
                        "UPDATE_PASSWORD"    // User must set password on first login
                ))
                .build();
        
        // Create user
        String userId = keycloakClient.createUser(targetRealm, authConfig, userRequest);
        
        // Send action email (verify email + set password)
        try {
            keycloakClient.executeActionsEmail(
                    targetRealm,
                    authConfig,
                    userId,
                    List.of("VERIFY_EMAIL", "UPDATE_PASSWORD")
            );
            logger.info("Sent OTP/action email to user: {}", username);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to user {}: {}", username, e.getMessage());
            // Don't fail the whole operation if email fails
        }
        
        logger.info("Created user with email OTP: {} (ID: {})", username, userId);
        
        return new KeycloakUserResponse(userId, username, email, false);
    }
    
    @Override
    public void sendVerificationEmail(Map<String, String> authConfig, String keycloakUserId) {
        logger.info("Sending verification email to user: {}", keycloakUserId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        keycloakClient.sendVerifyEmail(targetRealm, authConfig, keycloakUserId);
        
        logger.info("Sent verification email to user: {}", keycloakUserId);
    }
    
    @Override
    public void setUserEnabled(Map<String, String> authConfig, String keycloakUserId, boolean enabled) {
        logger.info("Setting user {} enabled status to: {}", keycloakUserId, enabled);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        // Get current user data
        JsonNode user = keycloakClient.getUser(targetRealm, authConfig, keycloakUserId);
        
        // Build update request with only enabled field changed
        KeycloakUserRequest updateRequest = KeycloakUserRequest.builder()
                .username(user.get("username").asText())
                .email(user.has("email") ? user.get("email").asText() : null)
                .enabled(enabled)
                .build();
        
        keycloakClient.updateUser(targetRealm, authConfig, keycloakUserId, updateRequest);
        
        logger.info("Updated user {} enabled status to: {}", keycloakUserId, enabled);
    }

    @Override
    public void updateUserPassword(Map<String, String> authConfig, String keycloakUserId, String password, boolean temporary) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password must not be empty");
        }
        logger.info("Updating password for Keycloak user: {}", keycloakUserId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        keycloakClient.resetUserPassword(targetRealm, authConfig, keycloakUserId, password, temporary);
        logger.info("Password updated for Keycloak user: {}", keycloakUserId);
    }

    @Override
    public Optional<String> findUserIdByUsername(Map<String, String> authConfig, String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        return keycloakClient.findUserIdByUsername(targetRealm, authConfig, username);
    }
    
    @Override
    public void deleteClient(Map<String, String> authConfig, String keycloakClientId) {
        logger.info("Deleting client: {}", keycloakClientId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        keycloakClient.deleteClient(targetRealm, authConfig, keycloakClientId);
        
        logger.info("Deleted client: {}", keycloakClientId);
    }
    
    @Override
    public void deleteUser(Map<String, String> authConfig, String keycloakUserId) {
        logger.info("Deleting user: {}", keycloakUserId);
        String targetRealm = extractRealmFromIssuer(getRequired(AuthProperties.ISSUER, authConfig, "tenant auth_config"));
        
        keycloakClient.deleteUser(targetRealm, authConfig, keycloakUserId);
        
        logger.info("Deleted user: {}", keycloakUserId);
    }

    private static String getRequired(AuthProperties key, Map<String, String> authConfig, String ctx) {
        if (authConfig == null || authConfig.isEmpty()) {
            throw new IllegalStateException("auth_config is empty for " + ctx);
        }
        String value = authConfig.get(key.value());
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing " + key.value() + " in auth_config for " + ctx);
        }
        return value;
    }

    private static String extractRealmFromIssuer(String issuer) {
        // ожидаем: .../realms/{realm}
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
        // если вдруг issuer продолжает путь (например /protocol/...),
        // отрежем лишнее
        int slash = after.indexOf('/');
        return slash >= 0 ? after.substring(0, slash) : after;
    }
}

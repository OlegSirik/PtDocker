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
import ru.pt.auth.configuration.KeycloakProperties;
import ru.pt.auth.model.keycloak.KeycloakClientRequest;
import ru.pt.auth.model.keycloak.KeycloakCredential;
import ru.pt.auth.model.keycloak.KeycloakUserRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakServiceImpl.class);
    
    private final KeycloakAdminClient keycloakClient;
    private final KeycloakProperties properties;
    
    @Override
    public KeycloakClientResponse createConfidentialClient(String clientId, String clientName, List<String> redirectUris) {
        logger.info("Creating confidential client in Keycloak: {}", clientId);
        
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
        String keycloakClientId = keycloakClient.createClient(properties.getDefaultRealm(), clientRequest);
        
        // Get client secret
        String secret = keycloakClient.getClientSecret(properties.getDefaultRealm(), keycloakClientId);
        
        logger.info("Created confidential client: {} with ID: {}", clientId, keycloakClientId);
        
        return new KeycloakClientResponse(keycloakClientId, clientId, secret, clientName);
    }
    
    @Override
    public String regenerateClientSecret(String keycloakClientId) {
        logger.info("Regenerating secret for client: {}", keycloakClientId);
        
        String newSecret = keycloakClient.regenerateClientSecret(properties.getDefaultRealm(), keycloakClientId);
        
        logger.info("Successfully regenerated secret for client: {}", keycloakClientId);
        return newSecret;
    }
    
    @Override
    public KeycloakUserResponse createUserWithPassword(String username, String email, String password, boolean emailVerified) {
        logger.info("Creating user with password: {}", username);
        
        // Build user request
        KeycloakUserRequest userRequest = KeycloakUserRequest.builder()
                .username(username)
                .email(email)
                .enabled(true)
                .emailVerified(emailVerified)
                .credentials(List.of(KeycloakCredential.password(password, false)))
                .build();
        
        // Create user
        String userId = keycloakClient.createUser(properties.getDefaultRealm(), userRequest);
        
        // Send verification email if not already verified
        if (!emailVerified) {
            try {
                keycloakClient.sendVerifyEmail(properties.getDefaultRealm(), userId);
                logger.info("Sent verification email to user: {}", username);
            } catch (Exception e) {
                logger.warn("Failed to send verification email to user {}: {}", username, e.getMessage());
            }
        }
        
        logger.info("Created user with password: {} (ID: {})", username, userId);
        
        return new KeycloakUserResponse(userId, username, email, emailVerified);
    }
    
    @Override
    public KeycloakUserResponse createUserWithEmailOtp(String username, String email) {
        logger.info("Creating user with email OTP: {}", username);
        
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
        String userId = keycloakClient.createUser(properties.getDefaultRealm(), userRequest);
        
        // Send action email (verify email + set password)
        try {
            keycloakClient.executeActionsEmail(
                    properties.getDefaultRealm(),
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
    public void sendVerificationEmail(String keycloakUserId) {
        logger.info("Sending verification email to user: {}", keycloakUserId);
        
        keycloakClient.sendVerifyEmail(properties.getDefaultRealm(), keycloakUserId);
        
        logger.info("Sent verification email to user: {}", keycloakUserId);
    }
    
    @Override
    public void setUserEnabled(String keycloakUserId, boolean enabled) {
        logger.info("Setting user {} enabled status to: {}", keycloakUserId, enabled);
        
        // Get current user data
        JsonNode user = keycloakClient.getUser(properties.getDefaultRealm(), keycloakUserId);
        
        // Build update request with only enabled field changed
        KeycloakUserRequest updateRequest = KeycloakUserRequest.builder()
                .username(user.get("username").asText())
                .email(user.has("email") ? user.get("email").asText() : null)
                .enabled(enabled)
                .build();
        
        keycloakClient.updateUser(properties.getDefaultRealm(), keycloakUserId, updateRequest);
        
        logger.info("Updated user {} enabled status to: {}", keycloakUserId, enabled);
    }
    
    @Override
    public void deleteClient(String keycloakClientId) {
        logger.info("Deleting client: {}", keycloakClientId);
        
        keycloakClient.deleteClient(properties.getDefaultRealm(), keycloakClientId);
        
        logger.info("Deleted client: {}", keycloakClientId);
    }
    
    @Override
    public void deleteUser(String keycloakUserId) {
        logger.info("Deleting user: {}", keycloakUserId);
        
        keycloakClient.deleteUser(properties.getDefaultRealm(), keycloakUserId);
        
        logger.info("Deleted user: {}", keycloakUserId);
    }
}

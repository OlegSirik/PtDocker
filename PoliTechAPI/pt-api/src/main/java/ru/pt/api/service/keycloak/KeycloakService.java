package ru.pt.api.service.keycloak;

import ru.pt.api.dto.keycloak.KeycloakClientResponse;
import ru.pt.api.dto.keycloak.KeycloakUserResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing Keycloak clients and users via REST API
 */
public interface KeycloakService {
    
    /**
     * Create a new confidential client in Keycloak
     * 
     * @param clientId Client ID (e.g., "client_123")
     * @param clientName Client name
     * @param redirectUris List of valid redirect URIs
     * @return KeycloakClientResponse with client details and secret
     */
    KeycloakClientResponse createConfidentialClient(Map<String, String> authConfig, String clientId, String clientName, List<String> redirectUris);
    
    /**
     * Create a new public client in Keycloak
     * 
     * @param clientId Client ID (e.g., "client_123")
     * @param clientName Client name
     * @param redirectUris List of valid redirect URIs
     * @return KeycloakClientResponse with client details and secret
     */
    KeycloakClientResponse createPublicClient(Map<String, String> authConfig, String clientId, String clientName, List<String> redirectUris);
    
    /**
     * Regenerate client secret
     * 
     * @param keycloakClientId Keycloak internal client UUID
     * @return New client secret
     */
    String regenerateClientSecret(Map<String, String> authConfig, String keycloakClientId);
    
    /**
     * Create user with password authentication
     * 
     * @param username Username
     * @param email Email address
     * @param password Password
     * @param emailVerified Whether email should be marked as verified
     * @return KeycloakUserResponse with user details
     */
    KeycloakUserResponse createUserWithPassword(Map<String, String> authConfig, String username, String email, String password, boolean emailVerified);
    
    /**
     * Create user with email OTP (passwordless)
     * Sends verification email with link to set password
     * 
     * @param username Username
     * @param email Email address
     * @return KeycloakUserResponse with user details
     */
    KeycloakUserResponse createUserWithEmailOtp(Map<String, String> authConfig, String username, String email);
    
    /**
     * Send email verification to user
     * 
     * @param keycloakUserId Keycloak internal user UUID
     */
    void sendVerificationEmail(Map<String, String> authConfig, String keycloakUserId);
    
    /**
     * Enable or disable user
     * 
     * @param keycloakUserId Keycloak internal user UUID
     * @param enabled Whether user should be enabled
     */
    void setUserEnabled(Map<String, String> authConfig, String keycloakUserId, boolean enabled);

    /**
     * Установить пароль существующему пользователю Keycloak (internal user id).
     *
     * @param temporary если true — пользователь должен сменить пароль при первом входе
     */
    void updateUserPassword(Map<String, String> authConfig, String keycloakUserId, String password, boolean temporary);

    /**
     * Найти internal id пользователя Keycloak по username (точное совпадение, Admin API).
     */
    Optional<String> findUserIdByUsername(Map<String, String> authConfig, String username);
    
    /**
     * Delete client from Keycloak
     * 
     * @param keycloakClientId Keycloak internal client UUID
     */
    void deleteClient(Map<String, String> authConfig, String keycloakClientId);
    
    /**
     * Delete user from Keycloak
     * 
     * @param keycloakUserId Keycloak internal user UUID
     */
    void deleteUser(Map<String, String> authConfig, String keycloakUserId);
}

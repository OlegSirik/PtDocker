package ru.pt.api.service.keycloak;

import ru.pt.api.dto.keycloak.KeycloakClientResponse;
import ru.pt.api.dto.keycloak.KeycloakUserResponse;

import java.util.List;

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
    KeycloakClientResponse createConfidentialClient(String clientId, String clientName, List<String> redirectUris);
    
    /**
     * Regenerate client secret
     * 
     * @param keycloakClientId Keycloak internal client UUID
     * @return New client secret
     */
    String regenerateClientSecret(String keycloakClientId);
    
    /**
     * Create user with password authentication
     * 
     * @param username Username
     * @param email Email address
     * @param password Password
     * @param emailVerified Whether email should be marked as verified
     * @return KeycloakUserResponse with user details
     */
    KeycloakUserResponse createUserWithPassword(String username, String email, String password, boolean emailVerified);
    
    /**
     * Create user with email OTP (passwordless)
     * Sends verification email with link to set password
     * 
     * @param username Username
     * @param email Email address
     * @return KeycloakUserResponse with user details
     */
    KeycloakUserResponse createUserWithEmailOtp(String username, String email);
    
    /**
     * Send email verification to user
     * 
     * @param keycloakUserId Keycloak internal user UUID
     */
    void sendVerificationEmail(String keycloakUserId);
    
    /**
     * Enable or disable user
     * 
     * @param keycloakUserId Keycloak internal user UUID
     * @param enabled Whether user should be enabled
     */
    void setUserEnabled(String keycloakUserId, boolean enabled);
    
    /**
     * Delete client from Keycloak
     * 
     * @param keycloakClientId Keycloak internal client UUID
     */
    void deleteClient(String keycloakClientId);
    
    /**
     * Delete user from Keycloak
     * 
     * @param keycloakUserId Keycloak internal user UUID
     */
    void deleteUser(String keycloakUserId);
}

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
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.auth.configuration.KeycloakProperties;
import ru.pt.auth.model.keycloak.*;

import java.util.List;
import java.util.Map;

/**
 * Low-level REST client for Keycloak Admin API
 */
@Component
public class KeycloakAdminClient {
    
    private static final Logger logger = LoggerFactory.getLogger(KeycloakAdminClient.class);
    
    private final KeycloakProperties properties;
    private final RestTemplate restTemplate;
    
    public KeycloakAdminClient(KeycloakProperties properties, 
                               @Qualifier("keycloakRestTemplate") RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }
    
    /**
     * Get admin access token
     */
    public String getAdminToken() {
        String url = properties.getServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token";
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", properties.getClientId());
        body.add("username", properties.getUsername());
        body.add("password", properties.getPassword());
        
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
    public String createClient(String realm, KeycloakClientRequest clientRequest) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/clients";
        
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
    public String getClientSecret(String realm, String clientId) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/clients/" + clientId + "/client-secret";
        
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
    public String regenerateClientSecret(String realm, String clientId) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/clients/" + clientId + "/client-secret";
        
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
    public String createUser(String realm, KeycloakUserRequest userRequest) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/users";
        
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
     * Get user by ID
     */
    public JsonNode getUser(String realm, String userId) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/users/" + userId;
        
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
    public void sendVerifyEmail(String realm, String userId) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/users/" + userId + "/send-verify-email";
        
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
    public void executeActionsEmail(String realm, String userId, List<String> actions) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/users/" + userId + "/execute-actions-email";
        
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
    public void updateUser(String realm, String userId, KeycloakUserRequest userRequest) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/users/" + userId;
        
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
     * Delete client
     */
    public void deleteClient(String realm, String clientId) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/clients/" + clientId;
        
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
    public void deleteUser(String realm, String userId) {
        String token = getAdminToken();
        String url = properties.getServerUrl() + "/admin/realms/" + realm + "/users/" + userId;
        
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

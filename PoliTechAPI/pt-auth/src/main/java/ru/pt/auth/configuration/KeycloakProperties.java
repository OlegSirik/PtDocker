package ru.pt.auth.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak.admin")
public class KeycloakProperties {
    
    /**
     * Keycloak server URL (e.g., http://localhost:8000)
     */
    private String serverUrl = "http://localhost:8000";
    
    /**
     * Admin realm name (usually 'master')
     */
    private String realm = "master";
    
    /**
     * Admin username
     */
    private String username = "admin";
    
    /**
     * Admin password
     */
    private String password = "admin";
    
    /**
     * Admin client ID (usually 'admin-cli')
     */
    private String clientId = "admin-cli";
    
    /**
     * Default realm for creating clients and users
     */
    private String defaultRealm = "politech";
    
    /**
     * Connection timeout in milliseconds
     */
    private int connectionTimeout = 10000;
    
    /**
     * Read timeout in milliseconds
     */
    private int readTimeout = 10000;
}

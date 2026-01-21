package ru.pt.api.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakClientResponse {
    
    /**
     * Keycloak internal client UUID
     */
    private String id;
    
    /**
     * Client ID (client_id)
     */
    private String clientId;
    
    /**
     * Client secret
     */
    private String secret;
    
    /**
     * Client name
     */
    private String name;
}

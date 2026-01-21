package ru.pt.api.dto.keycloak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserResponse {
    
    /**
     * Keycloak internal user UUID
     */
    private String id;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Email
     */
    private String email;
    
    /**
     * Whether email is verified
     */
    private Boolean emailVerified;
}

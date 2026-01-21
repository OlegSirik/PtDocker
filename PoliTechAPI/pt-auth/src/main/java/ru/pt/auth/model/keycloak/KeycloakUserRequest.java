package ru.pt.auth.model.keycloak;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class KeycloakUserRequest {
    
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private Boolean emailVerified;
    private List<KeycloakCredential> credentials;
    private List<String> requiredActions;
    private Map<String, List<String>> attributes;
}

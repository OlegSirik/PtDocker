package ru.pt.auth.model.keycloak;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class KeycloakClientRequest {
    
    private String clientId;
    private String name;
    private String description;
    private Boolean enabled;
    private Boolean publicClient;
    private Boolean serviceAccountsEnabled;
    private Boolean standardFlowEnabled;
    private Boolean directAccessGrantsEnabled;
    private Boolean implicitFlowEnabled;
    private List<String> redirectUris;
    private List<String> webOrigins;
    private String protocol;
    private Map<String, String> attributes;
}

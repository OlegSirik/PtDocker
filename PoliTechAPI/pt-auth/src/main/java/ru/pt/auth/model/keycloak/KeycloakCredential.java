package ru.pt.auth.model.keycloak;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeycloakCredential {
    
    private String type;
    private String value;
    private Boolean temporary;
    
    public static KeycloakCredential password(String password, boolean temporary) {
        return KeycloakCredential.builder()
                .type("password")
                .value(password)
                .temporary(temporary)
                .build();
    }
}

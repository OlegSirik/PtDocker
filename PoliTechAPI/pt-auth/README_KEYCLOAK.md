# Keycloak Integration - REST API Only

Pure REST API integration with Keycloak (no SDK dependencies).

## Architecture

**pt-api** - Interfaces & DTOs (contract):
- `ru.pt.api.service.keycloak.KeycloakService` - Service interface
- `ru.pt.api.dto.keycloak.*` - Response DTOs

**pt-auth** - Implementation (internal):
- `ru.pt.auth.service.KeycloakServiceImpl` - Service implementation
- `ru.pt.auth.client.KeycloakAdminClient` - REST API client
- `ru.pt.auth.configuration.*` - Configuration
- `ru.pt.auth.model.keycloak.*` - Internal request models

## Configuration

```yaml
keycloak:
  admin:
    server-url: http://localhost:8000
    realm: master
    username: admin
    password: admin
    client-id: admin-cli
    default-realm: politech
    connection-timeout: 10000
    read-timeout: 10000
```

## Usage

```java
@Service
@RequiredArgsConstructor
public class ClientService {
    
    private final KeycloakService keycloakService;
    
    public void createClientInKeycloak(ClientEntity client) {
        KeycloakClientResponse kc = keycloakService.createConfidentialClient(
            "client_" + client.getId(),
            client.getName(),
            List.of("http://localhost:3000/callback")
        );
        
        // Store keycloak client ID and secret
        client.setKeycloakClientId(kc.getId());
        client.setKeycloakSecret(encrypt(kc.getSecret()));
    }
    
    public void createUserWithPassword(String username, String email, String password) {
        KeycloakUserResponse user = keycloakService.createUserWithPassword(
            username, email, password, true
        );
    }
    
    public void inviteUser(String username, String email) {
        // Sends invitation email with OTP setup link
        KeycloakUserResponse user = keycloakService.createUserWithEmailOtp(
            username, email
        );
    }
}
```

## Features

✅ Create confidential OAuth2 clients  
✅ Regenerate client secrets  
✅ Create users with password  
✅ Create users with email OTP (passwordless invitation)  
✅ Send email verification  
✅ Enable/disable users  
✅ Delete clients and users  

## No Dependencies Required

Uses only Spring's `RestTemplate` - no Keycloak SDK needed!

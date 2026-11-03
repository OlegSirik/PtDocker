package ru.pt.auth.security.strategy;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.repository.AccountTokenRepository;
import ru.pt.auth.repository.ClientRepository;
import ru.pt.auth.security.context.RequestContext;

@Component
public class ApiKeyIdentityStrategy implements IdentitySourceStrategy {

    private final AccountTokenRepository accountTokenRepository;
    private final ClientRepository clientRepository;
    private final RequestContext requestContext;

    public ApiKeyIdentityStrategy(AccountTokenRepository accountTokenRepository,
                                  ClientRepository clientRepository,
                                  RequestContext requestContext) {
        this.accountTokenRepository = accountTokenRepository;
        this.clientRepository = clientRepository;
        this.requestContext = requestContext;
    }

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.APIKEY;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {
        String apiKey = request.getHeader("X-Api-Key");
        if (apiKey == null || apiKey.isBlank()) {
            throw new BadCredentialsException("X-Api-Key header is required");
        }

        String tenantCode = requestContext.getTenant();
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new BadCredentialsException("Tenant is required for API key authentication");
        }

        var tokenEntity = accountTokenRepository.findByTokenAndTenantCode(apiKey, tenantCode)
                .orElseThrow(() -> new BadCredentialsException("Invalid API key"));
        // Avoid lazy AccountEntity.client — read authClientId via client_id on the token row
        var client = clientRepository.findById(tokenEntity.getClientId())
                .orElseThrow(() -> new BadCredentialsException("Client not found for API key"));
        String clientAuthId = client.getAuthClientId();
        if (clientAuthId == null || clientAuthId.isBlank()) {
            throw new BadCredentialsException("Client authClientId missing for API key");
        }

        requestContext.setTenant(tenantCode);
        requestContext.setClient(clientAuthId);
        requestContext.setAccount(tokenEntity.getAccountId());
        // No login for API key - account is resolved directly
    }
}

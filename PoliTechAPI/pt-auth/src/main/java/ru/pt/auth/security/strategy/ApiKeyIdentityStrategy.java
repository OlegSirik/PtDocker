package ru.pt.auth.security.strategy;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.AccountTokenRepository;
import ru.pt.auth.security.context.RequestContext;

@Component
public class ApiKeyIdentityStrategy implements IdentitySourceStrategy {

    private final AccountTokenRepository accountTokenRepository;
    private final AccountRepository accountRepository;
    private final RequestContext requestContext;

    public ApiKeyIdentityStrategy(AccountTokenRepository accountTokenRepository,
                                  AccountRepository accountRepository,
                                  RequestContext requestContext) {
        this.accountTokenRepository = accountTokenRepository;
        this.accountRepository = accountRepository;
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

        var tokenEntity = accountTokenRepository.findByTokenAndTenantCodeWithClientAndAccount(apiKey, tenantCode)
                .orElseThrow(() -> new BadCredentialsException("Invalid API key"));
        var accountEntity = accountRepository.findById(tokenEntity.getAccountId())
                .orElseThrow(() -> new BadCredentialsException("Account not found for API key"));
        var clientAuthId = accountEntity.getClient().getAuthClientId();

        requestContext.setTenant(tenantCode);
        requestContext.setClient(clientAuthId);
        requestContext.setAccount(tokenEntity.getAccountId());
        // No login for API key - account is resolved directly
    }
}

package ru.pt.auth.security.strategy;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;
//import ru.pt.auth.security.RequestContext;
import ru.pt.auth.security.strategy.IdentitySourceStrategy;
//import ru.pt.auth.service.ApiKeyService;

@Component
public class ApiKeyIdentityStrategy implements IdentitySourceStrategy {

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.API_KEY;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
/* 
        ApiKeyEntity key = apiKeyService.validate(apiKey);

        RequestContext ctx = RequestContext.current();
        ctx.setClientId(key.getClientId());
        ctx.setAccountId(key.getAccountId());
*/
        }
}

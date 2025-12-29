package ru.pt.auth.security.strategy;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.security.context.RequestContext;

@Component
public class KeycloakIdentityStrategy implements IdentitySourceStrategy {

    private final RequestContext requestContext;

    public KeycloakIdentityStrategy(RequestContext requestContext) {
            this.requestContext = requestContext;
    }

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.KEYCLOAK;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {

        //Jwt jwt = extractAndValidateJwt(request);

        //requestContext.setExternalSubject(jwt.getSubject());
        //requestContext.setUserLogin(jwt.getClaim("email"));
        //requestContext.setClientId(jwt.getClaim("azp"));
    }
}


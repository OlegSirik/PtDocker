package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;

import org.springframework.stereotype.Component;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@Component
public class HeaderAuthenticationStrategy implements IdentitySourceStrategy {

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.HEADERS;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {

        throw new AuthenticationCredentialsNotFoundException(
                "HEADERS auth is disabled. Use LOCAL_JWT, JWT or KEYCLOAK.");
    }
}


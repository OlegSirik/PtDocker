package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Component
public class NoAuthenticationStrategy implements IdentitySourceStrategy {

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.NONE;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {
    }
}

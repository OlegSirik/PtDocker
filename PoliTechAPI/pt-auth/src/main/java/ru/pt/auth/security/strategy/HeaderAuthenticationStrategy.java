package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import ru.pt.auth.model.AuthType;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Component
public class HeaderAuthenticationStrategy implements IdentitySourceStrategy {

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.HEADERS;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {

        String clientId = request.getHeader("X-Client-Id");
        String userId   = request.getHeader("X-User-Id");
        String accountId = request.getHeader("X-Account-Id");
        
        if (clientId == null || userId == null) {
            throw new AuthenticationCredentialsNotFoundException("Headers required");
        }

        
    }
}


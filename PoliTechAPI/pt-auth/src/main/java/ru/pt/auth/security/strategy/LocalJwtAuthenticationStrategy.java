package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.security.JwtTokenUtil;
import ru.pt.auth.security.UserDetailsServiceImpl;
import ru.pt.auth.security.context.RequestContext;

@Component
public class LocalJwtAuthenticationStrategy implements IdentitySourceStrategy {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final RequestContext requestContext;

    public LocalJwtAuthenticationStrategy(
            JwtTokenUtil jwtTokenUtil,
            UserDetailsServiceImpl userDetailsService,
            RequestContext requestContext) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.requestContext = requestContext;
    }

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.LOCAL_JWT;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null) {
            throw new BadCredentialsException("JWT token missing");
        }

        if (!jwtTokenUtil.validateToken(jwt)) {
            throw new BadCredentialsException("Invalid JWT token");
        }

        String username = jwtTokenUtil.getUsernameFromToken(jwt);
        if (username == null) {
            throw new BadCredentialsException("JWT does not contain username");
        }
        String clientId = jwtTokenUtil.getClaimFromToken(jwt, "client_id");
        if (clientId == null) {
            throw new BadCredentialsException("JWT does not contain client_id");
        }

        requestContext.setClient(clientId);
        requestContext.setLogin(username);

        String accountId = request.getHeader("X-Account-Id");
        if (accountId != null && !accountId.isEmpty()) {
            try {
                Long.parseLong(accountId);
            } catch (NumberFormatException e) {
                throw new BadCredentialsException("X-Account-Id must be a number");
            }
        }
        requestContext.setAccount(accountId);
    }

    /**
     * Извлекает JWT токен из заголовка Authorization
     * Поддерживает формат: "Bearer <token>"
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}


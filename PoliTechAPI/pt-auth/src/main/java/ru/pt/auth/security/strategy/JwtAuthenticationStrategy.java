package ru.pt.auth.security.strategy;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Tenant;
import ru.pt.api.dto.refs.TenantAuthType;
import ru.pt.api.service.auth.TenantConfig;
import ru.pt.auth.model.AuthProperties;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.security.JwtTokenUtil;
import ru.pt.auth.security.UserDetailsServiceImpl;
import ru.pt.auth.security.context.RequestContext;

@Component
public class JwtAuthenticationStrategy implements IdentitySourceStrategy {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationStrategy.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final RequestContext requestContext;
    private final TenantConfig tenantConfig;


    public JwtAuthenticationStrategy(
            JwtTokenUtil jwtTokenUtil,
            UserDetailsServiceImpl userDetailsService,
            RequestContext requestContext,
            TenantConfig tenantConfig) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
        this.requestContext = requestContext;
        this.tenantConfig = tenantConfig;
    }

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.JWT;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) 
    {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null) {
            throw new BadCredentialsException("JWT token missing");
        }

        // Try per-tenant auth_config-based validation first (when configured),
        // otherwise fall back to legacy JwtTokenUtil.validateToken behavior.
        String tenantCode = requestContext.getTenant();
        boolean validated = false;
        if (tenantCode != null && !tenantCode.isBlank()) {
            try {
                Tenant tenant = tenantConfig.getTenant(tenantCode);
                if (TenantAuthType.JWT.equals(tenant.authType())
                        && tenant.authConfig() != null
                        && !tenant.authConfig().isEmpty()) {

                    String expectedIssuer = tenant.authConfig().get(AuthProperties.ISSUER.value());
                    String jwksUri = tenant.authConfig().get(AuthProperties.JWKS_URI.value());

                    // For now we still rely on JwtTokenUtil for basic checks and
                    // enforce issuer match if configured. JWKS integration can be
                    // plugged in here later using expectedIssuer + jwksUri.
                    if (!jwtTokenUtil.validateToken(jwt)) {
                        throw new BadCredentialsException("Invalid JWT token");
                    }
                    if (expectedIssuer != null) {
                        String tokenIssuer = jwtTokenUtil.getClaimFromToken(jwt, "iss");
                        if (tokenIssuer == null || !expectedIssuer.equals(tokenIssuer)) {
                            throw new BadCredentialsException("JWT issuer mismatch");
                        }
                    }
                    validated = true;
                }
            } catch (Exception e) {
                logger.warn("Per-tenant JWT validation failed for tenant {}: {}", tenantCode, e.getMessage());
                // fall through to legacy validation
            }
        }

        if (!validated) {
            if (!jwtTokenUtil.validateToken(jwt)) {
                throw new BadCredentialsException("Invalid JWT token");
            }
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

        String xAccountId = request.getHeader("X-Account-Id");
        if (xAccountId != null && !xAccountId.isEmpty()) {
            try {
                Long accountId = Long.parseLong(xAccountId);
                requestContext.setAccount(accountId);
            } catch (NumberFormatException e) {
                throw new BadCredentialsException("X-Account-Id must be a number");
            }
        }
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

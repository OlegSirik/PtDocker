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
import ru.pt.auth.security.context.RequestContext;

@Component
public class KeycloakIdentityStrategy implements IdentitySourceStrategy {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakIdentityStrategy.class);

    private final RequestContext requestContext;
    private final TenantConfig tenantConfig;
    private final JwtTokenUtil jwtTokenUtil;

    public KeycloakIdentityStrategy(
            RequestContext requestContext,
            TenantConfig tenantConfig,
            JwtTokenUtil jwtTokenUtil) {
        this.requestContext = requestContext;
        this.tenantConfig = tenantConfig;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean supports(AuthType type) {
        return type == AuthType.KEYCLOAK;
    }

    @Override
    public void resolveIdentity(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        if (jwt == null) {
            throw new BadCredentialsException("JWT token missing");
        }

        String tenantCode = requestContext.getTenant();
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new BadCredentialsException("Tenant not resolved for Keycloak authentication");
        }

        try {
            Tenant tenant = tenantConfig.getTenant(tenantCode);
            if (!TenantAuthType.KEYCLOAK.equals(tenant.authType())) {
                throw new BadCredentialsException("AuthType is not KEYCLOAK for tenant " + tenantCode);
            }

            // Basic claims-based validation using per-tenant auth_config
            if (tenant.authConfig() != null && !tenant.authConfig().isEmpty()) {
                String expectedIssuer = tenant.authConfig().get(AuthProperties.ISSUER.value());
                String expectedAudience = tenant.authConfig().get(AuthProperties.AUDIENCE.value());

                // Check expiration using common util (it only inspects exp claim)
                if (jwtTokenUtil.isTokenExpired(jwt)) {
                    throw new BadCredentialsException("JWT token is expired");
                }

                // Issuer check if configured
                if (expectedIssuer != null) {
                    String tokenIssuer = jwtTokenUtil.getClaimFromToken(jwt, "iss");
                    if (tokenIssuer == null || !expectedIssuer.equals(tokenIssuer)) {
                        throw new BadCredentialsException("JWT issuer mismatch");
                    }
                }

                // Audience / azp check if configured
                if (expectedAudience != null) {
                    String azp = jwtTokenUtil.getClaimFromToken(jwt, "azp");
                    String aud = jwtTokenUtil.getClaimFromToken(jwt, "aud");
                    if (!expectedAudience.equals(azp) && !expectedAudience.equals(aud)) {
                        throw new BadCredentialsException("JWT audience mismatch");
                    }
                }
            }

            // Map Keycloak claims into RequestContext
            String preferredUsername = jwtTokenUtil.getClaimFromToken(jwt, "preferred_username");
            String email = jwtTokenUtil.getClaimFromToken(jwt, "email");
            String sub = jwtTokenUtil.getClaimFromToken(jwt, "sub");

            String login = preferredUsername != null ? preferredUsername
                    : (email != null ? email : sub);
            if (login == null || login.isBlank()) {
                throw new BadCredentialsException("JWT does not contain username/email/subject");
            }

            String clientId = jwtTokenUtil.getClaimFromToken(jwt, "azp");
            if (clientId == null) {
                clientId = jwtTokenUtil.getClaimFromToken(jwt, "client_id");
            }

            requestContext.setLogin(login);
            if (clientId != null && !clientId.isBlank()) {
                requestContext.setClient(clientId);
            }

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Keycloak identity resolution failed for tenant {}: {}", tenantCode, e.getMessage());
            throw new BadCredentialsException("Invalid Keycloak JWT token", e);
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}

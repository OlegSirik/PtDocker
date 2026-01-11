package ru.pt.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pt.auth.configuration.SecurityConfigurationProperties;
import ru.pt.auth.security.context.RequestContext;
import java.io.IOException;

public class IdentityResolutionFilter extends AbstractSecurityFilter {

    private static final Logger logger = LoggerFactory.getLogger(IdentityResolutionFilter.class);
    private final UserDetailsService userDetailsService;
    private final RequestContext requestContext;

    public IdentityResolutionFilter(SecurityConfigurationProperties securityProperties, UserDetailsService userDetailsService, RequestContext requestContext) {
        super(securityProperties);
        this.userDetailsService = userDetailsService;
        this.requestContext = requestContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        logger.debug("IdentityResolutionFilter: Processing request {}", request.getRequestURI());

        // Если уже аутентифицированы — ничего не делаем
        // Пропускаем публичные URL
        if (isPublicRequest(request)) {
            logger.debug("IdentityResolutionFilter: Skipping - public request");
            filterChain.doFilter(request, response);
            return;
        }

        Authentication existingAuth =
                SecurityContextHolder.getContext().getAuthentication();

        logger.debug("IdentityResolutionFilter: Existing auth = {} (authenticated: {})", 
                     existingAuth, existingAuth != null ? existingAuth.isAuthenticated() : false);

        if (existingAuth != null && existingAuth.isAuthenticated()) {
            logger.debug("IdentityResolutionFilter: Skipping - already authenticated");
            filterChain.doFilter(request, response);
            return;
        }

        String tenant = requestContext.getTenant();
        String accountId = requestContext.getAccount();

        logger.debug("IdentityResolutionFilter: tenant={}, accountId={}", tenant, accountId);

        // Недостаточно данных — значит это public или pre-auth запрос
        if (tenant == null || accountId == null) {
            logger.debug("IdentityResolutionFilter: Skipping - missing tenant ({}) or accountId ({})", tenant, accountId);
            filterChain.doFilter(request, response);
            return;
        }

        logger.debug("IdentityResolutionFilter: Proceeding with authentication for accountId={}", accountId);

        // Загружаем UserDetails (account-aware)
        try {
            logger.debug("IdentityResolutionFilter: Loading user details for accountId={}", accountId);
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(accountId);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("IdentityResolutionFilter: Successfully authenticated user: {}", userDetails.getUsername());
        } catch (Exception e) {
            logger.error("IdentityResolutionFilter: Failed to authenticate accountId={}, error: {}", accountId, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}


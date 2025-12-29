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
import org.springframework.stereotype.Component;
import ru.pt.auth.configuration.SecurityConfigurationProperties;
import ru.pt.auth.security.context.RequestContext;
import java.io.IOException;

public class IdentityResolutionFilter extends AbstractSecurityFilter {

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

        // Если уже аутентифицированы — ничего не делаем
        // Пропускаем публичные URL
        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication existingAuth =
                SecurityContextHolder.getContext().getAuthentication();

        if (existingAuth != null && existingAuth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenant = requestContext.getTenant();
        String accountId = requestContext.getAccount();

        // Недостаточно данных — значит это public или pre-auth запрос
        if (tenant == null || accountId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Загружаем UserDetails (account-aware)
        try {
            String a = "123";
            
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(accountId);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (Exception e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        return;
    }

        filterChain.doFilter(request, response);
    }
}


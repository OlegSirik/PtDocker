package ru.pt.auth.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.model.ClientSecurityConfig;
import ru.pt.auth.model.TenantSecurityConfig;
import ru.pt.auth.security.context.RequestContext;
import ru.pt.auth.security.strategy.IdentitySourceStrategy;
import ru.pt.auth.service.AccountResolverService;
import ru.pt.auth.service.TenantSecurityConfigService;
import ru.pt.auth.configuration.SecurityConfigurationProperties;
import ru.pt.auth.service.ClientSecurityConfigService;

public class AccountResolutionFilter extends AbstractSecurityFilter {

    private final TenantSecurityConfigService configService;
    private final List<IdentitySourceStrategy> strategies;
    private final AccountResolverService accountResolverService;
    private final RequestContext requestContext;
    private final ClientSecurityConfigService clientSecurityConfigService;

    public AccountResolutionFilter(
        SecurityConfigurationProperties securityProperties,
        TenantSecurityConfigService configService,
        List<IdentitySourceStrategy> strategies,
        AccountResolverService accountResolverService,
        RequestContext requestContext,
        ClientSecurityConfigService clientSecurityConfigService
    ) {
        super(securityProperties);
        this.configService = configService;
        this.strategies = strategies;
        this.accountResolverService = accountResolverService;
        this.requestContext = requestContext;
        this.clientSecurityConfigService = clientSecurityConfigService;
    }

    /* 
     * Фильтр для разрешения аккаунта account_id.
     * Если аккаунт не разрешен, то фильтр выбрасывает ошибку 401.
     * Если аккаунт разрешен, то фильтр пропускает запрос.
     * Все необходимые переменные берутся из контекста запроса и там же сохраняются.
     * По итогу в контексте лежит account_id = sprint.username
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
    */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // Пропускаем публичные URL
        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenant = requestContext.getTenant();
        if (tenant == null) {
            filterChain.doFilter(request, response);
            return;
        }

        TenantSecurityConfig resolved;
        boolean deletedAccess = false;
        try {
            resolved = configService.getConfig(tenant);
        } catch (NotFoundException ex) {
            // Soft-deleted tenant: resolve auth config, SYS_ADMIN-only after identity
            resolved = configService.getDeletedTenantConfig(tenant)
                    .orElseThrow(() -> ex);
            deletedAccess = true;
        }
        requestContext.setDeletedTenantAccess(deletedAccess);
        final TenantSecurityConfig config = resolved;
        AuthType authType = config.authType();

        String clientId = request.getHeader("X-Client-ID");
        if (clientId != null && !clientId.isEmpty()) {
            try {
                ClientSecurityConfig clientConfig = clientSecurityConfigService.getConfig(tenant, clientId);
                if (clientConfig != null && clientConfig.authType() != null) {
                    authType = clientConfig.authType();
                }
            } catch (IllegalArgumentException ignore) {
                // X-Client-ID from another tenant (e.g. leftover header on /sys/…) — keep tenant authType
            }
        }
        if (authType == AuthType.NONE) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final AuthType aType = authType;

        IdentitySourceStrategy strategy = strategies.stream()
            .filter(s -> s.supports(aType))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No AuthenticationStrategy for " + aType));
    
        String xAccountId = request.getHeader("X-Account-Id");
        if (xAccountId != null && !xAccountId.isEmpty()) {
            try {
                Long accountId = Long.parseLong(xAccountId);
                requestContext.setAccount(accountId);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("X-Account-Id must be a number");
            }
        }
    
        try {
            strategy.resolveIdentity(request);

            accountResolverService.resolveAccounts();
            
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }


}


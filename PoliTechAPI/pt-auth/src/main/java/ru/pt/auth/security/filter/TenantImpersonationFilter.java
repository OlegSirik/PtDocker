package ru.pt.auth.security.filter;

import ru.pt.auth.configuration.SecurityConfigurationProperties;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.security.UserDetailsServiceImpl;
import ru.pt.auth.security.context.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

public class TenantImpersonationFilter extends AbstractSecurityFilter {

    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final RequestContext requestContext;

    public TenantImpersonationFilter(SecurityConfigurationProperties securityProperties,
                                     UserDetailsServiceImpl userDetailsServiceImpl,
                                     RequestContext requestContext) {
        super(securityProperties);
        this.userDetailsServiceImpl = userDetailsServiceImpl;
        this.requestContext = requestContext;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
    
        // Пропускаем публичные URL
        if (isPublicRequest(request)) {
            chain.doFilter(request, response);
            return;
        }
    
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    
        if (auth == null || !auth.isAuthenticated()) {
            if (requestContext.isDeletedTenantAccess()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "Deleted tenant is only accessible for SYS_ADMIN");
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            chain.doFilter(request, response);
            return;
        }
        
        UserDetailsImpl user = (UserDetailsImpl) principal;

        // Soft-deleted path tenant: only SYS_ADMIN (sys) may continue
        if (requestContext.isDeletedTenantAccess() && !"SYS_ADMIN".equals(user.getUserRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Deleted tenant is only accessible for SYS_ADMIN");
            return;
        }

        // SYS impersonation
        if ("SYS_ADMIN".equals(user.getUserRole())) {
            String impersonatedTenant = request.getHeader("X-Imp-Tenant");
            if (impersonatedTenant != null && !impersonatedTenant.isEmpty()) {
                //user.setImpersonatedTenantCode(impersonatedTenant); 
                user = userDetailsServiceImpl.impersonateSysAdmin(user, impersonatedTenant);
            }
        }
        chain.doFilter(request, response);
    }
}

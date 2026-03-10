package ru.pt.auth.security.filter;

import ru.pt.auth.configuration.SecurityConfigurationProperties;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.security.UserDetailsServiceImpl;
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

    public TenantImpersonationFilter(SecurityConfigurationProperties securityProperties, UserDetailsServiceImpl userDetailsServiceImpl) {
        super(securityProperties);
        this.userDetailsServiceImpl = userDetailsServiceImpl;
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
            chain.doFilter(request, response);
            return;
        }
        
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            chain.doFilter(request, response);
            return;
        }
        
        UserDetailsImpl user = (UserDetailsImpl) principal;

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

package ru.pt.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.pt.auth.configuration.SecurityConfigurationProperties;
import ru.pt.auth.security.context.RequestContext;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantResolutionFilter extends AbstractSecurityFilter {

    private final Logger logger = LoggerFactory.getLogger(TenantResolutionFilter.class);

    /**
     * TenantContextFilter — технический фильтр
     * Его задача:
     * извлечь tenant
     * положить в контекст.
     * @param request the request
     * @return the tenant code
     */

    private final RequestContext requestContext;

    public TenantResolutionFilter(SecurityConfigurationProperties securityProperties, RequestContext requestContext) {
        super(securityProperties);
        this.requestContext = requestContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        // Пропускаем публичные URL
        if (isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tenant = extractTenant(request);

        requestContext.setTenant(tenant);

        logger.debug("Tenant: {}", tenant);

        try {
            filterChain.doFilter(request, response);
        } finally {
            requestContext.clear();
        }
    }

    private String extractTenant(HttpServletRequest request) {
        String[] segments = request.getRequestURI().split("/");
        String tenant = null;

        for (int i = 0; i < segments.length - 2; i++) {
            if ("api".equals(segments[i])
                    && isVersionSegment(segments[i + 1])) {
                tenant = segments[i + 2];
                break;
            }
        }
        if (tenant == null || tenant.isEmpty()) {
            tenant = null;
        }
        return tenant;
    }

    private boolean isVersionSegment(String segment) {
        if (segment == null || segment.length() < 2 || segment.charAt(0) != 'v') {
            return false;
        }
        for (int i = 1; i < segment.length(); i++) {
            if (!Character.isDigit(segment.charAt(i))) {
                return false;
            }
        }
        return true;
    }    
}

package ru.pt.auth.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.pt.auth.configuration.SecurityConfigurationProperties;

public abstract class AbstractSecurityFilter extends OncePerRequestFilter {

    protected final SecurityConfigurationProperties securityProperties;

    protected AbstractSecurityFilter(SecurityConfigurationProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * Проверяет, является ли запрос публичным.
     * Если да — фильтр может пропустить обработку.
     */
    protected boolean isPublicRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        for (String pattern : securityProperties.getPublicUrls()) {
            String regex = pattern.replace(".", "\\.").replace("*", ".*");
            if (uri.matches(regex)) {
                return true;
            }
        }
        return false;
    }
}

package ru.pt.api.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HealthLogSuppressFilter extends OncePerRequestFilter {

    private static final String ACTUATOR_HEALTH_PREFIX = "/actuator/health";
    private static final String MDC_KEY_SUPPRESS_LOGS = "suppressLogs";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        boolean suppress = uri != null && uri.startsWith(ACTUATOR_HEALTH_PREFIX);
        if (suppress) {
            MDC.put(MDC_KEY_SUPPRESS_LOGS, "true");
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (suppress) {
                MDC.remove(MDC_KEY_SUPPRESS_LOGS);
            }
        }
    }
}


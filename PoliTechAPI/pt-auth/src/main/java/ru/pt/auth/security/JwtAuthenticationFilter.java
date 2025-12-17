package ru.pt.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.service.AccountLoginService;
import ru.pt.auth.service.ClientService;
import ru.pt.auth.service.TenantService;

import java.io.IOException;
import java.util.Optional;

/**
 * JWT фильтр для аутентификации пользователей по токену.
 * Извлекает JWT из заголовка Authorization, парсит логин и загружает пользователя из БД.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final TenantService tenantService;
    private final ClientService clientService;
    private final AccountLoginService accountLoginService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
                                  TenantService tenantService,
                                  ClientService clientService,
                                  AccountLoginService accountLoginService,
                                  UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.clientService = clientService;
        this.tenantService = tenantService;
        this.accountLoginService = accountLoginService;
        this.userDetailsService = userDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        try {
            String tenantCode = extractTenantCodeFromRequest(request).orElseThrow(
                    () -> new BadRequestException("Tenant code is missing in the URL")
            );
            TenantEntity tenantEntity = tenantService.findByCode(tenantCode).orElseThrow(
                    () -> new BadRequestException("Tenant with code " + tenantCode + " not found")
            );
            if (tenantEntity.getTokenAuth()) {

                String jwt = extractJwtFromRequest(request);

                if (jwt != null && jwtTokenUtil.validateToken(jwt)) {
                    String username = jwtTokenUtil.getUsernameFromToken(jwt);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        // Создаем аутентификацию без проверки пароля
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        logger.debug("User {} authenticated successfully via JWT", username);
                    }
                }
            } else {
                String clientId = request.getHeader("Partner-Client-Id");
                String userLogin = request.getHeader("Partner-User-Id");
                if (clientId != null && userLogin != null) {
                    if (accountLoginService.validateUserLoginAndClientId(userLogin, clientId).isEmpty()) {
                        logger.error("User {} is not associated with client_id {}", userLogin, clientId);
                        throw new BadRequestException(
                            "User " + userLogin + " is not authorized for client " + clientId
                        );
                    }

                    clientService.findByClientId(clientId).ifPresent(clientEntity -> {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        logger.debug("User {} authenticated successfully via Partner headers", userLogin);
                    });
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication", e);
        }

        filterChain.doFilter(request, response);
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
    private Optional<String> extractTenantCodeFromRequest(HttpServletRequest request) {
        String[] segments = request.getRequestURI().split("/");
        for (int i = 0; i < segments.length - 2; i++) {
            if ("api".equals(segments[i])
                    && isVersionSegment(segments[i + 1])) {
                return segments[i + 2] != null
                        ? Optional.of(segments[i + 2])
                        : Optional.empty();
            }
        }
        return Optional.empty();
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


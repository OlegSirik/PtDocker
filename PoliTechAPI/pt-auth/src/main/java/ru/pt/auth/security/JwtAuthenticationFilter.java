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
import ru.pt.auth.entity.ClientEntity;
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
                // Получаем заголовки Partner-Client-Id и Partner-User-Id (могут отсутствовать)
                String clientId = request.getHeader("Partner-Client-Id");
                String userLogin = request.getHeader("Partner-User-Id");

                // Нормализуем пустые строки к null
                if (clientId != null && clientId.isBlank()) {
                    clientId = null;
                }
                if (userLogin != null && userLogin.isBlank()) {
                    userLogin = null;
                }

                // Если оба заголовка отсутствуют — выдаем ошибку авторизации
                if (clientId == null && userLogin == null) {
                    logger.error("No authentication provided. Either JWT token or Partner headers are required");
                    throw new BadRequestException("Authentication required. Please provide JWT token or Partner headers");
                }
                // Если заполнен хотя бы один из заголовков — проверяем и валидируем
                else {
                    if (clientId != null && userLogin != null) {
                        final String finalClientId = clientId;
                        final String finalUserLogin = userLogin;

                        ClientEntity clientEntity = clientService.findByClientId(finalClientId).orElseThrow(
                            () -> new BadRequestException("Client with id " + finalClientId + " not found")
                        );

                        // Проверяем соответствие user_login в БД acc_logins и связь с client_id в acc_account_logins
                        if (accountLoginService.validateUserLoginAndClientId(finalUserLogin, finalClientId, tenantCode).isEmpty()) {
                            logger.error("User {} is not associated with client_id {} or tenantCode {}", finalUserLogin, finalClientId, tenantCode);
                            throw new BadRequestException(
                                    "User " + finalUserLogin + " is not authorized for client " + finalClientId + " in tenant " + tenantCode
                            );
                        }
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(finalUserLogin);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            logger.debug("User {} authenticated successfully via Partner headers for client {}", finalUserLogin, finalClientId);
                        } catch (Exception e) {
                            logger.error("Failed to load user details for user {}", finalUserLogin, e);
                            throw new BadRequestException("User " + finalUserLogin + " not found or authentication failed");
                        }
                    }
                    else if (clientId != null) {
                        final String finalClientId = clientId;

                        ClientEntity clientEntity = clientService.findByClientId(finalClientId).orElseThrow(
                            () -> new BadRequestException("Client with id " + finalClientId + " not found")
                        );

                        if (clientEntity.getDefaultAccountId() == null) {
                            logger.error("Client {} does not have default account configured", finalClientId);
                            throw new BadRequestException("Client " + finalClientId + " does not have default account configured");
                        }

                        String defaultUserLogin = clientService.getDefaultAccountLogin(clientEntity.getDefaultAccountId()).orElseThrow(
                            () -> new BadRequestException("Default account login not found for client " + finalClientId)
                        );

                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(defaultUserLogin);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            logger.debug("Default user {} authenticated successfully for client {}", defaultUserLogin, finalClientId);
                        } catch (Exception e) {
                            logger.error("Failed to load default user details for client {}", finalClientId, e);
                            throw new BadRequestException("Failed to authenticate with default account for client " + finalClientId);
                        }
                    }
                    else {
                        final String finalUserLogin = userLogin;

                        // Проверяем существование user_login в БД acc_logins для текущего тенанта
                        if (!accountLoginService.validateUserLoginInTenant(finalUserLogin, tenantCode)) {
                            logger.error("User {} not found in tenant {} or is deleted", finalUserLogin, tenantCode);
                            throw new BadRequestException("User " + finalUserLogin + " is not registered in tenant " + tenantCode);
                        }

                        // Если все проверки прошли успешно, устанавливаем аутентификацию
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(finalUserLogin);

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            logger.debug("User {} authenticated successfully via Partner-User-Id for tenant {}", finalUserLogin, tenantCode);
                        } catch (Exception e) {
                            logger.error("Failed to load user details for user {}", finalUserLogin, e);
                            throw new BadRequestException("User " + finalUserLogin + " authentication failed");
                        }
                    }
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

package ru.pt.api.security;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.SetPasswordRequest;
import ru.pt.auth.model.LoginRequest;
import ru.pt.auth.model.TokenRequest;
import ru.pt.auth.model.TokenResponse;
import ru.pt.auth.security.JwtTokenUtil;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.LoginManagementService;
import ru.pt.auth.service.SimpleAuthService;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для работы с текущим пользователем.
 * Демонстрирует использование UserDetailsImpl и SecurityContextHelper с JWT авторизацией.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final SecurityContextHelper securityContextHelper;
    private final JwtTokenUtil jwtTokenUtil;
    private final SimpleAuthService simpleAuthService;
    private final LoginManagementService loginManagementService;

    public AuthenticationController(SecurityContextHelper securityContextHelper,
                                   JwtTokenUtil jwtTokenUtil,
                                   LoginManagementService loginManagementService,
                                   SimpleAuthService simpleAuthService) {
        this.securityContextHelper = securityContextHelper;
        this.loginManagementService = loginManagementService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.simpleAuthService = simpleAuthService;
    }

    /**
     * Получить информацию о текущем пользователе через @AuthenticationPrincipal
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    private ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("tenantCode", userDetails.getTenantCode());
        response.put("accountId", userDetails.getAccountId());
        response.put("accountName", userDetails.getAccountName());
        response.put("clientId", userDetails.getClientId());
        response.put("clientName", userDetails.getClientName());
        response.put("userRole", userDetails.getUserRole());
        response.put("productRoles", userDetails.getProductRoles());
        response.put("authorities", userDetails.getAuthorities());
        response.put("isDefault", userDetails.isDefault());

        return ResponseEntity.ok(response);
    }

    /**
     * Получить информацию о текущем пользователе через SecurityContextHelper
     */
    @GetMapping("/context")
    @SecurityRequirement(name = "bearerAuth")
    private ResponseEntity<Map<String, Object>> getCurrentUserContext() {
        Map<String, Object> response = new HashMap<>();

        securityContextHelper.getCurrentUser().ifPresent(user -> {
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("accountId", user.getAccountId());
            response.put("clientId", user.getClientId());
            response.put("tenantCode", user.getTenantCode());
        });

        response.put("isAuthenticated", securityContextHelper.isAuthenticated());

        return ResponseEntity.ok(response);
    }

    /**
     * Проверка прав доступа к продукту
     */
    @GetMapping("/check-product-access")
    public ResponseEntity<Map<String, Object>> checkProductAccess(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Map<String, Object> response = new HashMap<>();

        // Примеры проверки прав
        response.put("canReadProduct", userDetails.canPerformAction("PRODUCT_CODE", "READ"));
        response.put("canQuoteProduct", userDetails.canPerformAction("PRODUCT_CODE", "QUOTE"));
        response.put("canPolicyProduct", userDetails.canPerformAction("PRODUCT_CODE", "POLICY"));

        return ResponseEntity.ok(response);
    }

    /**
     * Простая аутентификация с логином и паролем (без Keycloak)
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        if (request.getUserLogin() == null || request.getUserLogin().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new TokenResponse(null, "User login is required"));
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new TokenResponse(null, "Password is required"));
        }

        String token = simpleAuthService.authenticate(
                request.getUserLogin(),
                request.getPassword(),
                request.getClientId()
        );

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenResponse(null, "Invalid credentials"));
        }

        TokenResponse response = new TokenResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(24L * 60 * 60); // 24 часа в секундах

        return ResponseEntity.ok(response);
    }

    /**
     * Генерация JWT токена для пользователя
     * POST /api/auth/token
     */
    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@RequestBody TokenRequest request) {
        String token = jwtTokenUtil.createToken(request.getUserLogin(), request.getClientId());

        if (token == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate token");
            error.put("message", "User not found or not associated with specified client");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new TokenResponse(null, error.get("message")));
        }

        TokenResponse response = new TokenResponse();
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setExpiresIn(24L * 60 * 60); // 24 часа в секундах

        return ResponseEntity.ok(response);
    }

    /**
     * Генерация refresh токена для пользователя
     * POST /api/auth/refresh-token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> generateRefreshToken(@RequestBody TokenRequest request) {
        String refreshToken = jwtTokenUtil.refreshToken(request.getUserLogin(), request.getClientId());

        if (refreshToken == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate refresh token");
            error.put("message", "User not found or not associated with specified client");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new TokenResponse(null, error.get("message")));
        }

        TokenResponse response = new TokenResponse();
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(30L * 24 * 60 * 60); // 30 дней в секундах

        return ResponseEntity.ok(response);
    }

    /**
     * Установка/обновление пароля для пользователя
     * POST /api/auth/set-password
     * Требуется роль SYS_ADMIN
     */
    @PostMapping("/set-password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> setPassword(@RequestBody SetPasswordRequest request) {
        try {
            loginManagementService.setPassword(
                    request.getUserLogin(),
                    request.getPassword(),
                    request.getClientId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password set successfully for user: " + request.getUserLogin());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(403).body(response);
        }
    }
}

package ru.pt.api.security;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.SetPasswordRequest;
import ru.pt.api.dto.auth.Principal;
import ru.pt.api.service.auth.AccountService;
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
import java.util.List;
import java.util.stream.Collectors;
import ru.pt.api.dto.auth.PrincipalAccount;
/**
 * Контроллер для работы с текущим пользователем.
 * Демонстрирует использование UserDetailsImpl и SecurityContextHelper с JWT авторизацией.
 */
@RequiredArgsConstructor
@RestController
//@RequestMapping("/api/auth")
@RequestMapping("/api/v1/{tenantCode}/auth")
public class AuthenticationController {

    private final SecurityContextHelper securityContextHelper;
    private final JwtTokenUtil jwtTokenUtil;
    private final SimpleAuthService simpleAuthService;
    private final LoginManagementService loginManagementService;
    private final AccountService accountService;

    /**
     * Получить информацию о текущем пользователе через @AuthenticationPrincipal
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    private ResponseEntity<Principal> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        var accounts = accountService.getAllMyAccounts(userDetails.getTenantCode(), userDetails.getClientId(), userDetails.getUsername());
        
        List<PrincipalAccount> principalAccounts = accounts.stream()
            .map(account -> new PrincipalAccount(account.id(), account.nodeType(), account.name()))
            .collect(Collectors.toList());

        Principal principal = new Principal(
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getTenantCode(),
            userDetails.getAccountName(),
            userDetails.getClientId(),
            userDetails.getClientName(),
            userDetails.getUserRole(),
            userDetails.getProductRoles(),
            userDetails.getAuthorities(),
            userDetails.isDefault(),
            userDetails.getAccountId(),
            principalAccounts
        );

        return ResponseEntity.ok(principal);
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
            @PathVariable String tenantCode,
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
     * POST /api/v1/{tenantCode}/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
        @PathVariable String tenantCode,
        @RequestBody LoginRequest request) {

        String tCode = tenantCode.toLowerCase();

        if (request.getUserLogin() == null || request.getUserLogin().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new TokenResponse(null, "User login is required"));
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new TokenResponse(null, "Password is required"));
        }

        String token = simpleAuthService.authenticate(
                tCode,
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
    public ResponseEntity<TokenResponse> generateToken(
        @PathVariable String tenantCode,
        @RequestBody TokenRequest request) {
        String token = jwtTokenUtil.createToken(tenantCode, request.getClientId(), request.getUserLogin());

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
    public ResponseEntity<TokenResponse> generateRefreshToken(
        @PathVariable String tenantCode,
        @RequestBody TokenRequest request) {
        String refreshToken = jwtTokenUtil.refreshToken(tenantCode, request.getClientId(), request.getUserLogin());

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
//    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> setPassword(
        @PathVariable String tenantCode,
        @RequestBody SetPasswordRequest request,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String tCode = tenantCode.toLowerCase();

        if (!tCode.equalsIgnoreCase(userDetails.getTenantCode())) {
            return ResponseEntity.status(403).body(new HashMap<String, Object>() {{
                put("error", "Tenant code mismatch");
            }});
        }
        try {
            boolean authorized = false;

            String username =  userDetails.getUsername();
            //String tenantCode = userDetails.getTenantCode();
            Long clientId = userDetails.getClientId();
            String userRole = userDetails.getUserRole();
    
            if (("SYS_ADMIN".equals(userRole)) || ("TNT_ADMIN".equals(userRole))) {
                // SYS admin может все
                authorized = true;
                tCode = request.getTenantCode();
            }
            if (username.equals(request.getUserLogin())) {
                // Пользователь может изменить свой пароль
                authorized = true;
            }
            // ToDo прочие кейсы по изменению пароля

            if (authorized) {

                loginManagementService.setPassword(
                        tCode,
                        request.getUserLogin(),
                        request.getPassword()
                );
            

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Password set successfully for user: " + request.getUserLogin());

                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Not authorized to set password for user: " + request.getUserLogin());
                return ResponseEntity.status(403).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(403).body(response);
        }
    }
}

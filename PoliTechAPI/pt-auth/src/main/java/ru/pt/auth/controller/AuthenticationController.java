package ru.pt.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

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

    public AuthenticationController(SecurityContextHelper securityContextHelper) {
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Получить информацию о текущем пользователе через @AuthenticationPrincipal
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("username", userDetails.getUsername());
        response.put("tenantId", userDetails.getTenantId());
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
    public ResponseEntity<Map<String, Object>> getCurrentUserContext() {
        Map<String, Object> response = new HashMap<>();

        securityContextHelper.getCurrentUser().ifPresent(user -> {
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("accountId", user.getAccountId());
            response.put("clientId", user.getClientId());
            response.put("tenantId", user.getTenantId());
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
     * Endpoint доступный только для пользователей с ролью ADMIN
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("You have ADMIN role!");
    }

    /**
     * Endpoint доступный для пользователей с правом чтения продукта
     */
    @GetMapping("/product-read")
    @PreAuthorize("hasRole('PRODUCT_CODE_READ')")
    public ResponseEntity<String> productRead() {
        return ResponseEntity.ok("You can read PRODUCT_CODE!");
    }
}


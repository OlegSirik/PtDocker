package ru.pt.api.security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.SetPasswordRequest;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.LoginManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления паролями пользователей
 */
@RestController
@RequestMapping("/api/auth")
public class PasswordManagementController extends SecuredController {

    private final LoginManagementService loginManagementService;

    public PasswordManagementController(SecurityContextHelper securityContextHelper,
                                        LoginManagementService loginManagementService) {
        super(securityContextHelper);
        this.loginManagementService = loginManagementService;
    }

    /**
     * Установка/обновление пароля для пользователя
     * POST /api/auth/set-password
     * Требуется роль SYS_ADMIN
     */
    @PostMapping("/set-password")
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


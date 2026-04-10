package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.CreateLoginRequest;
import ru.pt.api.admin.dto.UpdateLoginRequest;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.model.LoginDto;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.LoginManagementService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления пользователями (логинами)
 * Работа по документации acc_logins.md
 *
 * URL Pattern: /api/v1/{tenantCode}/logins
 * tenantCode: pt, vsk, msg
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/logins")
public class LoginManagementController extends SecuredController {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final LoginManagementService loginManagementService;

    public LoginManagementController(SecurityContextHelper securityContextHelper,
                                    LoginManagementService loginManagementService) {
        super(securityContextHelper);
        this.loginManagementService = loginManagementService;
    }

    /**
     * ublic record LoginDto(
    Long id,
    Long clientId,
    String clientCode,
    String userLogin,
    String fullName,
    String position
) {}


     * Создание пользователя (логина)
     * POST /api/v1/{tenantCode}/logins
     */
    @PostMapping
    public ResponseEntity<LoginDto> createLogin(
            @PathVariable String tenantCode,
            @RequestBody CreateLoginRequest request) {
    
            return ResponseEntity.ok(loginManagementService.createLogin(
                    tenantCode,
                    request.getUserLogin(),
                    request.getFullName(),
                    request.getPosition()
            ));
    }

    /**
     * Обновление данных пользователя
     * PATCH /tnts/{tenantCode}/logins/{loginId}
     */
    @PatchMapping("/{loginId}")
    public ResponseEntity<LoginDto> updateLogin(
            @PathVariable String tenantCode,
            @PathVariable Long loginId,
            @RequestBody UpdateLoginRequest request) {
        
            return ResponseEntity.ok(loginManagementService.updateLogin(
                    tenantCode,
                    loginId,
                    request.getFullName(),
                    request.getPosition(),
                    request.getRecordStatus()
            ));
    }

    /**
     * Обновление данных пользователя
     * PATCH /tnts/{tenantCode}/logins/{loginId}
     */
    @PutMapping("/{loginId}")
    public ResponseEntity<LoginDto> updateLoginFull(
            @PathVariable String tenantCode,
            @PathVariable Long loginId,
            @RequestBody UpdateLoginRequest request) {
        
            return ResponseEntity.ok(loginManagementService.updateLogin(
                    tenantCode,
                    loginId,
                    request.getFullName(),
                    request.getPosition(),
                    request.getRecordStatus()
            ));
    }

    /**
     * Получение всех пользователей тенанта
     * GET /tnts/{tenantCode}/logins
     */
    @GetMapping
    public ResponseEntity<List<LoginDto>> getLogins(@PathVariable String tenantCode) {
            return ResponseEntity.ok(loginManagementService.getLoginsByTenant(tenantCode));
    }

    /**
     * Удаление пользователя (soft delete)
     * DELETE /api/v1/{tenantCode}/auth/logins/{loginId}
     */
    @DeleteMapping("/{loginId}")
    public ResponseEntity<Map<String, Object>> deleteLogin(
            @PathVariable String tenantCode,
            @PathVariable Long loginId) {
            loginManagementService.deleteLogin(tenantCode, loginId);
            return ResponseEntity.noContent().build();
    }

}


package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.CreateLoginRequest;
import ru.pt.api.admin.dto.LoginResponse;
import ru.pt.api.admin.dto.UpdateLoginRequest;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.model.LoginDto;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.LoginManagementService;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * Создание пользователя (логина)
     * POST /api/v1/{tenantCode}/logins
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'CLIENT_ADMIN''GROUP_ADMIN')")
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
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'CLIENT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<LoginDto> updateLogin(
            @PathVariable String tenantCode,
            @PathVariable Long loginId,
            @RequestBody UpdateLoginRequest request) {
        
            return ResponseEntity.ok(loginManagementService.updateLogin(
                    tenantCode,
                    loginId,
                    request.getFullName(),
                    request.getPosition(),
                    request.getIsDeleted()
            ));
    }

    /**
     * Обновление данных пользователя
     * PATCH /tnts/{tenantCode}/logins/{loginId}
     */
    @PutMapping("/{loginId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'CLIENT_ADMIN', 'GROUP_ADMIN', 'SALES')")
    public ResponseEntity<LoginDto> updateLoginFull(
            @PathVariable String tenantCode,
            @PathVariable Long loginId,
            @RequestBody UpdateLoginRequest request) {
        
            return ResponseEntity.ok(loginManagementService.updateLogin(
                    tenantCode,
                    loginId,
                    request.getFullName(),
                    request.getPosition(),
                    request.getIsDeleted()
            ));
    }

    /**
     * Получение всех пользователей тенанта
     * GET /tnts/{tenantCode}/logins
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN')")
    public ResponseEntity<List<LoginDto>> getLogins(@PathVariable String tenantCode) {
            return ResponseEntity.ok(loginManagementService.getLoginsByTenant(tenantCode));
    }

    /**
     * Удаление пользователя (soft delete)
     * DELETE /api/v1/{tenantCode}/auth/logins/{loginId}
     */
    @DeleteMapping("/{loginId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteLogin(
            @PathVariable String tenantCode,
            @PathVariable Long loginId) {
            loginManagementService.deleteLogin(tenantCode, loginId);
            return ResponseEntity.noContent().build();
    }

    /**
     * Преобразование LoginEntity в LoginResponse
     */
    private LoginResponse convertToResponse(LoginEntity login) {
        LoginResponse response = new LoginResponse();
        response.setId(String.valueOf(login.getId()));
        response.setTenantCode(login.getTenant().getCode());
        response.setUserLogin(login.getUserLogin());
        response.setFullName(login.getFullName());
        response.setPosition(login.getPosition());
        response.setIsDeleted(login.getIsDeleted());

        if (login.getCreatedAt() != null) {
            response.setCreatedAt(login.getCreatedAt().format(ISO_FORMATTER));
        }
        if (login.getUpdatedAt() != null) {
            response.setUpdatedAt(login.getUpdatedAt().format(ISO_FORMATTER));
        }

        return response;
    }
}


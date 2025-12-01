package ru.pt.api.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.CreateLoginRequest;
import ru.pt.api.admin.dto.LoginResponse;
import ru.pt.api.admin.dto.UpdateLoginRequest;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.security.SecurityContextHelper;
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
@RequestMapping("/api/v1/{tenantCode}/logins")
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
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<LoginResponse> createLogin(
            @PathVariable String tenantCode,
            @RequestBody CreateLoginRequest request) {
        try {
            requireAnyRole("SYS_ADMIN", "TNT_ADMIN", "GROUP_ADMIN");

            LoginEntity login = loginManagementService.createLogin(
                    tenantCode,
                    request.getUserLogin(),
                    request.getFullName(),
                    request.getPosition()
            );

            LoginResponse response = new LoginResponse();
            response.setId(String.valueOf(login.getId()));
            response.setTenantCode(String.valueOf(tenantCode));
            response.setUserLogin(login.getUserLogin());
            response.setFullName(login.getFullName());
            response.setPosition(login.getPosition());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Обновление данных пользователя
     * PATCH /tnts/{tenantCode}/logins/{loginId}
     */
    @PatchMapping("/{loginId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateLogin(
            @PathVariable String tenantCode,
            @PathVariable Long loginId,
            @RequestBody UpdateLoginRequest request) {
        try {
            LoginEntity login = loginManagementService.updateLogin(
                    tenantCode,
                    loginId,
                    request.getFullName(),
                    request.getPosition(),
                    request.getIsDeleted()
            );

            Map<String, Object> response = new HashMap<>();
            if (request.getFullName() != null) {
                response.put("fullName", login.getFullName());
            }
            if (request.getPosition() != null) {
                response.put("position", login.getPosition());
            }
            if (request.getIsDeleted() != null) {
                response.put("isDeleted", login.getIsDeleted());
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * Получение всех пользователей тенанта
     * GET /tnts/{tenantCode}/logins
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'TNT_ADMIN', 'GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<List<LoginResponse>> getLogins(@PathVariable String tenantCode) {
        try {
            requireAnyRole("SYS_ADMIN", "TNT_ADMIN", "GROUP_ADMIN", "PRODUCT_ADMIN");

            List<LoginEntity> logins = loginManagementService.getLoginsByTenant(tenantCode);

            List<LoginResponse> response = logins.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
        try {
            requireAnyRole("SYS_ADMIN", "TNT_ADMIN", "GROUP_ADMIN");

            LoginEntity login = loginManagementService.deleteLogin(tenantCode, loginId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", String.valueOf(login.getId()));
            response.put("userLogin", login.getUserLogin());
            response.put("isDeleted", login.getIsDeleted());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return handleException(e);
        }
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


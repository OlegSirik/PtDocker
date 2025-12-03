package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.Getter;
import lombok.Setter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Контроллер для управления администраторами всех уровней
 * TNT_ADMIN, GROUP_ADMIN, PRODUCT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/admins
 * tenantCode: pt, vsk, msg
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/admins")
public class AdminManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminManagementController(SecurityContextHelper securityContextHelper,
                                    AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.adminUserManagementService = adminUserManagementService;
    }

    // ========== SYS_ADMIN MANAGEMENT (SYS_ADMIN) ==========

    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя
     * POST /api/v1/{tenantCode}/admin/admins/sys-admins
     */
    @GetMapping("/sys-admins")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getSysAdmins() {
        try {
            List<AccountLoginEntity> accountLogins = adminUserManagementService.getSysAdmins();

            return ResponseEntity.ok(accountLogins.stream().map(accountLogin -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", accountLogin.getId());
                response.put("userLogin", accountLogin.getUserLogin());
                response.put("userRole", accountLogin.getUserRole());
                response.put("accountId", accountLogin.getAccount().getId());
                return response;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя
     * POST /api/v1/{tenantCode}/admin/admins/tnt-admins
     */
    @PostMapping("/sys-admins")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> createSysAdmin(
            @PathVariable String tenantCode,
            @RequestBody CreateAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createSysAdmin(
                    request.getUserLogin(),
                    request.getUserName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("accountId", accountLogin.getAccount().getId());

            return buildCreatedResponse(response, "SYS_ADMIN user created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ========== TNT_ADMIN MANAGEMENT (SYS_ADMIN) ==========

    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя
     * POST /api/v1/{tenantCode}/admin/admins/tnt-admins
     */
    @GetMapping("/tnt-admins")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTntAdmins(
            @PathVariable String tenantCode) {
        try {
            List<AccountLoginEntity> accountLogins = adminUserManagementService.getTntAdmins(tenantCode);

            return ResponseEntity.ok(accountLogins.stream().map(accountLogin -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", accountLogin.getId());
                response.put("userLogin", accountLogin.getUserLogin());
                response.put("userRole", accountLogin.getUserRole());
                response.put("accountId", accountLogin.getAccount().getId());
                return response;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя
     * POST /api/v1/{tenantCode}/admin/admins/tnt-admins
     */
    @PostMapping("/tnt-admins")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> createTntAdmin(
            @PathVariable String tenantCode,
            @RequestBody CreateTntAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createTntAdmin(
                    request.getTenantCode(),
                    request.getFullName(),
                    request.getUserLogin(),
                    request.getUserName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("accountId", accountLogin.getAccount().getId());

            return buildCreatedResponse(response, "TNT_ADMIN user created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * SYS_ADMIN: Удаление TNT_ADMIN пользователя
     * DELETE /api/v1/{tenantCode}/admin/admins/tnt-admins/{adminId}
     */
    @DeleteMapping("/tnt-admins/{adminId}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTntAdmin(
            @PathVariable String tenantCode,
            @PathVariable Long adminId) {
        try {

            adminUserManagementService.deleteTntAdmin(adminId);

            Map<String, Object> response = buildSimpleResponse("TNT_ADMIN user deleted successfully");
            response.put("adminId", adminId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ========== GROUP_ADMIN MANAGEMENT (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN: Создание GROUP_ADMIN пользователя
     * POST /api/v1/{tenantCode}/admin/admins/group-admins
     */
    @PostMapping("/group-admins")
    @PreAuthorize("hasRole('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createGroupAdmin(
            @PathVariable String tenantCode,
            @RequestBody CreateAdminRequest request) {
        try {

            AccountLoginEntity accountLogin = adminUserManagementService.createGroupAdmin(
                    request.getUserLogin(),
                    request.getUserName(),
                    request.getFullName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("accountId", accountLogin.getAccount().getId());

            return buildCreatedResponse(response, "GROUP_ADMIN user created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * TNT_ADMIN: Удаление GROUP_ADMIN пользователя
     * DELETE /api/v1/{tenantCode}/admin/admins/group-admins/{adminId}
     */
    @DeleteMapping("/group-admins/{adminId}")
    @PreAuthorize("hasRole('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteGroupAdmin(
            @PathVariable String tenantCode,
            @PathVariable Long adminId) {
        try {

            adminUserManagementService.deleteGroupAdmin(adminId);

            Map<String, Object> response = buildSimpleResponse("GROUP_ADMIN user deleted successfully");
            response.put("adminId", adminId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // ========== PRODUCT_ADMIN MANAGEMENT (GROUP_ADMIN) ==========

    /**
     * GROUP_ADMIN: Создание PRODUCT_ADMIN пользователя
     * POST /api/v1/{tenantCode}/admin/admins/product-admins
     */
    @PostMapping("/product-admins")
    @PreAuthorize("hasRole('GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> createProductAdmin(
            @PathVariable String tenantCode,
            @RequestBody CreateAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createProductAdmin(
                    request.getUserLogin(),
                    request.getUserName(),
                    request.getFullName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("fullName", accountLogin.getLogin().getFullName());
            response.put("userRole", accountLogin.getUserRole());
            response.put("accountId", accountLogin.getAccount().getId());

            return buildCreatedResponse(response, "PRODUCT_ADMIN user created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * GROUP_ADMIN: Редактирование PRODUCT_ADMIN пользователя
     * PUT /api/v1/{tenantCode}/admin/admins/product-admins/{adminId}
     */
    @PutMapping("/product-admins/{adminId}")
    @PreAuthorize("hasRole('GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProductAdmin(
            @PathVariable String tenantCode,
            @PathVariable Long adminId,
            @RequestBody UpdateAdminRequest request) {
        try {

            AccountLoginEntity accountLogin = adminUserManagementService.updateProductAdmin(
                    adminId,
                    request.getUserRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userRole", accountLogin.getUserRole());

            return buildSuccessResponse(response, "PRODUCT_ADMIN user updated successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // DTO Classes
    public static class CreateTntAdminRequest {
        private String tenantCode;
        private String userLogin;
        private String userName;
        private String fullName;

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public String getUserLogin() {
            return userLogin;
        }

        public void setUserLogin(String userLogin) {
            this.userLogin = userLogin;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String fullName) {
            this.userName = userName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }


    }

    public static class CreateAdminRequest {
        private String userLogin;
        private String userName;
        private String fullName;

        public String getUserLogin() {
            return userLogin;
        }

        public void setUserLogin(String userLogin) {
            this.userLogin = userLogin;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    public static class UpdateAdminRequest {
        private String userRole;

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }
    }
}

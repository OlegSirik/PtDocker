package ru.pt.api.createUsers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.ProductRoleEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления администраторами и пользователями
 * с контролем доступа по ролям
 */
@RestController
@RequestMapping("/api/admin")
public class CreateAdminsAndUsersController {

    private final AdminUserManagementService adminUserManagementService;

    public CreateAdminsAndUsersController(AdminUserManagementService adminUserManagementService) {
        this.adminUserManagementService = adminUserManagementService;
    }

    // ========== TENANT MANAGEMENT (SYS_ADMIN) ==========

    /**
     * SYS_ADMIN: Создание нового tenant
     * POST /api/admin/tenants
     */
    @PostMapping("/tenants")
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> createTenant(@RequestBody CreateTenantRequest request) {
        try {
            TenantEntity tenant = adminUserManagementService.createTenant(request.getTenantName());

            Map<String, Object> response = new HashMap<>();
            response.put("id", tenant.getId());
            response.put("name", tenant.getName());
            response.put("message", "Tenant created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     * DELETE /api/admin/tenants/{tenantId}
     */
    @DeleteMapping("/tenants/{tenantId}")
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTenant(@PathVariable Long tenantId) {
        try {
            adminUserManagementService.deleteTenant(tenantId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tenant deleted successfully");

            return ResponseEntity.ok(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ========== TNT_ADMIN MANAGEMENT (SYS_ADMIN) ==========

    /**
     * SYS_ADMIN: Создание TNT_ADMIN пользователя
     * POST /api/admin/tnt-admins
     */
    @PostMapping("/tnt-admins")
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> createTntAdmin(@RequestBody CreateAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createTntAdmin(
                    request.getTenantId(),
                    request.getUserLogin(),
                    request.getUserName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("message", "TNT_ADMIN user created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * SYS_ADMIN: Удаление TNT_ADMIN пользователя
     * DELETE /api/admin/tnt-admins/{accountLoginId}
     */
    @DeleteMapping("/tnt-admins/{accountLoginId}")
    @PreAuthorize("hasAuthority('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTntAdmin(@PathVariable Long accountLoginId) {
        try {
            adminUserManagementService.deleteTntAdmin(accountLoginId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "TNT_ADMIN user deleted successfully");

            return ResponseEntity.ok(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ========== GROUP_ADMIN MANAGEMENT (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN: Создание GROUP_ADMIN пользователя
     * POST /api/admin/group-admins
     */
    @PostMapping("/group-admins")
    @PreAuthorize("hasAuthority('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createGroupAdmin(@RequestBody CreateSimpleAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createGroupAdmin(
                    request.getUserLogin(),
                    request.getUserName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("message", "GROUP_ADMIN user created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * TNT_ADMIN: Удаление GROUP_ADMIN пользователя
     * DELETE /api/admin/group-admins/{accountLoginId}
     */
    @DeleteMapping("/group-admins/{accountLoginId}")
    @PreAuthorize("hasAuthority('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteGroupAdmin(@PathVariable Long accountLoginId) {
        try {
            adminUserManagementService.deleteGroupAdmin(accountLoginId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "GROUP_ADMIN user deleted successfully");

            return ResponseEntity.ok(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ========== PRODUCT_ADMIN MANAGEMENT (GROUP_ADMIN) ==========

    /**
     * GROUP_ADMIN: Создание PRODUCT_ADMIN пользователя
     * POST /api/admin/product-admins
     */
    @PostMapping("/product-admins")
    @PreAuthorize("hasAuthority('GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> createProductAdmin(@RequestBody CreateSimpleAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createProductAdmin(
                    request.getUserLogin(),
                    request.getUserName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("message", "PRODUCT_ADMIN user created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * GROUP_ADMIN: Редактирование PRODUCT_ADMIN пользователя
     * PUT /api/admin/product-admins/{accountLoginId}
     */
    @PutMapping("/product-admins/{accountLoginId}")
    @PreAuthorize("hasAuthority('GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProductAdmin(
            @PathVariable Long accountLoginId,
            @RequestBody UpdateProductAdminRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.updateProductAdmin(
                    accountLoginId,
                    request.getUserRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userRole", accountLogin.getUserRole());
            response.put("message", "PRODUCT_ADMIN user updated successfully");

            return ResponseEntity.ok(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ========== PRODUCT ROLES (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN: Выдача роли на продажу продукта
     * POST /api/admin/product-roles
     */
    @PostMapping("/product-roles")
    @PreAuthorize("hasAuthority('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> assignProductRole(@RequestBody AssignProductRoleRequest request) {
        try {
            ProductRoleEntity role = adminUserManagementService.assignProductRole(
                    request.getAccountId(),
                    request.getRoleProductId(),
                    request.getCanRead(),
                    request.getCanQuote(),
                    request.getCanPolicy(),
                    request.getCanAddendum(),
                    request.getCanCancel(),
                    request.getCanProlong()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", role.getId());
            response.put("accountId", role.getAccount().getId());
            response.put("roleProductId", role.getRoleProductId());
            response.put("message", "Product role assigned successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * TNT_ADMIN: Отзыв роли на продажу продукта
     * DELETE /api/admin/product-roles/{productRoleId}
     */
    @DeleteMapping("/product-roles/{productRoleId}")
    @PreAuthorize("hasAuthority('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> revokeProductRole(@PathVariable Long productRoleId) {
        try {
            adminUserManagementService.revokeProductRole(productRoleId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product role revoked successfully");

            return ResponseEntity.ok(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ========== HELPER METHODS ==========

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        return ResponseEntity.status(status).body(response);
    }

    // ========== DTO CLASSES ==========

    public static class CreateTenantRequest {
        private String tenantName;

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }
    }

    public static class CreateAdminRequest {
        private Long tenantId;
        private String userLogin;
        private String userName;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
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

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }

    public static class CreateSimpleAdminRequest {
        private String userLogin;
        private String userName;

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
    }

    public static class UpdateProductAdminRequest {
        private String userRole;

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }
    }

    public static class AssignProductRoleRequest {
        private Long accountId;
        private Long roleProductId;
        private Boolean canRead;
        private Boolean canQuote;
        private Boolean canPolicy;
        private Boolean canAddendum;
        private Boolean canCancel;
        private Boolean canProlong;

        // Getters and Setters
        public Long getAccountId() {
            return accountId;
        }

        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }

        public Long getRoleProductId() {
            return roleProductId;
        }

        public void setRoleProductId(Long roleProductId) {
            this.roleProductId = roleProductId;
        }

        public Boolean getCanRead() {
            return canRead;
        }

        public void setCanRead(Boolean canRead) {
            this.canRead = canRead;
        }

        public Boolean getCanQuote() {
            return canQuote;
        }

        public void setCanQuote(Boolean canQuote) {
            this.canQuote = canQuote;
        }

        public Boolean getCanPolicy() {
            return canPolicy;
        }

        public void setCanPolicy(Boolean canPolicy) {
            this.canPolicy = canPolicy;
        }

        public Boolean getCanAddendum() {
            return canAddendum;
        }

        public void setCanAddendum(Boolean canAddendum) {
            this.canAddendum = canAddendum;
        }

        public Boolean getCanCancel() {
            return canCancel;
        }

        public void setCanCancel(Boolean canCancel) {
            this.canCancel = canCancel;
        }

        public Boolean getCanProlong() {
            return canProlong;
        }

        public void setCanProlong(Boolean canProlong) {
            this.canProlong = canProlong;
        }
    }
}

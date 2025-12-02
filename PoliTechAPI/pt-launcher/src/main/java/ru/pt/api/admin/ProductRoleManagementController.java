package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.ProductRoleEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления ролями на продукты
 * Доступен для TNT_ADMIN и GROUP_ADMIN
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/admin/product-roles")
public class ProductRoleManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;

    public ProductRoleManagementController(SecurityContextHelper securityContextHelper,
                                           AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.adminUserManagementService = adminUserManagementService;
    }

    /**
     * TNT_ADMIN / GROUP_ADMIN: Выдача роли на продажу продукта
     * POST /api/admin/product-roles
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> assignProductRole(@RequestBody AssignProductRoleRequest request) {
        try {
            ProductRoleEntity role = adminUserManagementService.assignProductRole(
                    request.getAccountId(),
                    request.getRoleProductId(),
                    request.getRoleAccountId(),
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
            response.put("roleAccountId", role.getRoleAccount().getId());
            response.put("permissions", buildPermissions(role));
            response.put("message", "Product role assigned successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ForbiddenException | BadRequestException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * TNT_ADMIN / GROUP_ADMIN: Обновление роли на продукт
     * PUT /api/admin/product-roles/{productRoleId}
     */
    @PutMapping("/{productRoleId}")
    @PreAuthorize("hasAnyRole('TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateProductRole(
            @PathVariable Long productRoleId,
            @RequestBody UpdateProductRoleRequest request) {
        try {
            requireAnyRole("TNT_ADMIN", "GROUP_ADMIN");

            ProductRoleEntity role = adminUserManagementService.updateProductRole(
                    productRoleId,
                    request.getCanRead(),
                    request.getCanQuote(),
                    request.getCanPolicy(),
                    request.getCanAddendum(),
                    request.getCanCancel(),
                    request.getCanProlong()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", role.getId());
            response.put("permissions", buildPermissions(role));

            return buildSuccessResponse(response, "Product role updated successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * TNT_ADMIN / GROUP_ADMIN: Отзыв роли на продажу продукта
     * DELETE /api/admin/product-roles/{productRoleId}
     */
    @DeleteMapping("/{productRoleId}")
    @PreAuthorize("hasAnyRole('TNT_ADMIN', 'GROUP_ADMIN')")
    public ResponseEntity<Map<String, Object>> revokeProductRole(@PathVariable Long productRoleId) {
        try {
            requireAnyRole("TNT_ADMIN", "GROUP_ADMIN");

            adminUserManagementService.revokeProductRole(productRoleId);

            Map<String, Object> response = buildSimpleResponse("Product role revoked successfully");
            response.put("productRoleId", productRoleId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * TNT_ADMIN / GROUP_ADMIN / PRODUCT_ADMIN: Получить все роли на продукты для аккаунта
     * GET /api/admin/product-roles/account/{accountId}
     */
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('TNT_ADMIN', 'GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getProductRolesByAccount(@PathVariable Long accountId) {
        try {
            requireAnyRole("TNT_ADMIN", "GROUP_ADMIN", "PRODUCT_ADMIN");

            List<Map<String, Object>> roles = adminUserManagementService.getProductRolesByAccount(accountId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private Map<String, Boolean> buildPermissions(ProductRoleEntity role) {
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("canRead", role.getCanRead());
        permissions.put("canQuote", role.getCanQuote());
        permissions.put("canPolicy", role.getCanPolicy());
        permissions.put("canAddendum", role.getCanAddendum());
        permissions.put("canCancel", role.getCanCancel());
        permissions.put("canProlongate", role.getCanProlongate());
        return permissions;
    }

    // DTO Classes
    public static class AssignProductRoleRequest {
        private Long accountId;
        private Long roleProductId;
        private Long roleAccountId;
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

        public Long getRoleAccountId() {
            return roleAccountId;
        }

        public void setRoleAccountId(Long roleAccountId) {
            this.roleAccountId = roleAccountId;
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

    public static class UpdateProductRoleRequest {
        private Boolean canRead;
        private Boolean canQuote;
        private Boolean canPolicy;
        private Boolean canAddendum;
        private Boolean canCancel;
        private Boolean canProlong;

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


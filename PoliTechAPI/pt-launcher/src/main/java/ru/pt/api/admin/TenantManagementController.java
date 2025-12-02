package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления тенантами
 * Доступен только для SYS_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/tenants
 * tenantCode: pt, vsk, msg (глобальный для SYS_ADMIN операций)
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/tenants")
public class TenantManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;

    public TenantManagementController(SecurityContextHelper securityContextHelper,
                                     AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.adminUserManagementService = adminUserManagementService;
    }

    /**
     * SYS_ADMIN: Создание нового tenant
     * POST /api/v1/{tenantCode}/admin/tenants
     */
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> createTenant(
            @PathVariable String tenantCode,
            @RequestBody CreateTenantRequest request) {
        try {
            TenantEntity tenant = adminUserManagementService.createTenant(request.getTenantName(),
                    request.getTenantCode());

            Map<String, Object> response = new HashMap<>();
            response.put("id", tenant.getId());
            response.put("name", tenant.getName());
            response.put("createdAt", tenant.getCreatedAt());

            return buildCreatedResponse(response, "Tenant created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     * DELETE /api/v1/{tenantCode}/admin/tenants/{tenantResourceId}
     */
    @DeleteMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteTenant(
            @PathVariable String tenantCode) {
        try {
            adminUserManagementService.deleteTenant(tenantCode);

            Map<String, Object> response = buildSimpleResponse("Tenant deleted successfully");
            response.put("tenantCode", tenantCode);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // DTO Classes
    public static class CreateTenantRequest {
        private String tenantName;
        private String tenantCode;

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }
    }
}


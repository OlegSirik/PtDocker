package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     * SYS_ADMIN: Получить список всех tenant
     * GET /api/v1/{tenantCode}/admin/tenants
     */

    @GetMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getTenants() {
        try {
        List<TenantEntity> tenants = adminUserManagementService.getTenants();
        return ResponseEntity.ok(tenants.stream().map(tenant -> {
            Map<String, Object> tenantMap = new HashMap<>();
            tenantMap.put("id", tenant.getId());
            tenantMap.put("name", tenant.getName());
            tenantMap.put("code", tenant.getCode());
            tenantMap.put("createdAt", tenant.getCreatedAt());
                return tenantMap;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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
            TenantEntity tenant = adminUserManagementService.createTenant(request.getName(),
                    request.getCode());

            Map<String, Object> response = new HashMap<>();
            response.put("id", tenant.getId());
            response.put("name", tenant.getName());
            response.put("createdAt", tenant.getCreatedAt());
            response.put("code", tenant.getCode());

            return buildCreatedResponse(response, "Tenant created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * SYS_ADMIN: Создание нового tenant
     * POST /api/v1/{tenantCode}/admin/tenants
     */
    @PutMapping
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateTenant(
            @PathVariable String tenantCode,
            @RequestBody CreateTenantRequest request) {
        try {
            TenantEntity tenant = adminUserManagementService.updateTenant(request.getName(), request.getCode());

            Map<String, Object> response = new HashMap<>();
            response.put("id", tenant.getId());
            response.put("name", tenant.getName());
            response.put("createdAt", tenant.getCreatedAt());
            response.put("code", tenant.getCode());

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
        private String name;
        private String code;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }
}


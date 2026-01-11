package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.TenantService;

import java.util.List;
import ru.pt.api.dto.auth.Tenant;

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

    private final TenantService tenantService;

    public TenantManagementController(SecurityContextHelper securityContextHelper,
                                     TenantService tenantService) {
        super(securityContextHelper);
        this.tenantService = tenantService;
    }

    /**
     * SYS_ADMIN: Получить список всех tenant
     * GET /api/v1/{tenantCode}/admin/tenants
     */

    @GetMapping
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('TNT_ADMIN')")
    public ResponseEntity<List<Tenant>> getTenants() {
        List<Tenant> tenants = tenantService.getTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * SYS_ADMIN: Создание нового tenant
     * POST /api/v1/{tenantCode}/admin/tenants
     */
    @PostMapping
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('TNT_ADMIN')")
    public ResponseEntity<Tenant> createTenant(
            @PathVariable String tenantCode,
            @RequestBody Tenant tenantDto) {
        Tenant tenant = tenantService.createTenant(tenantDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tenant);
    }

    /**
     * SYS_ADMIN: Создание нового tenant
     * POST /api/v1/{tenantCode}/admin/tenants
     */
    @PutMapping("/{tid}")
    @PreAuthorize("hasRole('SYS_ADMIN') or hasRole('TNT_ADMIN')")
    public ResponseEntity<Tenant> updateTenant(
            @PathVariable String tenantCode,
            @RequestBody Tenant tenantDto) {

        Tenant tenant = tenantService.updateTenant(tenantDto);
        return ResponseEntity.ok(tenant);            
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     * DELETE /api/v1/{tenantCode}/admin/tenants/{tenantResourceId}
     */
    @DeleteMapping("/{tid}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable String tenantCode) {
        tenantService.deleteTenant(tenantCode);
        return ResponseEntity.noContent().build();
    }


}


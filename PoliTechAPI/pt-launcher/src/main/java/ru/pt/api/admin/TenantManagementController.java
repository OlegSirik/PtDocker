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

import ru.pt.api.admin.dto.TenantDto;

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
    public ResponseEntity<List<TenantDto>> getTenants() {
        try {
        List<TenantEntity> tenants = adminUserManagementService.getTenants();
        return ResponseEntity.ok(tenants.stream().map(tenant -> {
            TenantDto tenantDto = new TenantDto();
            tenantDto.setId(tenant.getId());
            tenantDto.setName(tenant.getName());
            tenantDto.setCode(tenant.getCode());
            tenantDto.setIsDeleted(tenant.getDeleted());
            tenantDto.setCreatedAt(tenant.getCreatedAt());
            tenantDto.setUpdatedAt(tenant.getUpdatedAt());
            return tenantDto;
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
    public ResponseEntity<TenantDto> createTenant(
            @PathVariable String tenantCode,
            @RequestBody TenantDto tenantDto) {
        try {

            TenantEntity tenantEntity = new TenantEntity();
              
            tenantEntity.setName(tenantDto.getName());
            tenantEntity.setCode(tenantDto.getCode());
            tenantEntity.setDeleted(tenantDto.getIsDeleted());
            tenantEntity.setCreatedAt(tenantDto.getCreatedAt());
            tenantEntity.setUpdatedAt(tenantDto.getUpdatedAt());

            TenantEntity tenant = adminUserManagementService.createTenant(tenantEntity);

            TenantDto response = new TenantDto();
            response.setId(tenant.getId());
            response.setName(tenant.getName());
            response.setCode(tenant.getCode());
            response.setIsDeleted(tenant.getDeleted());
            response.setCreatedAt(tenant.getCreatedAt());
            response.setUpdatedAt(tenant.getUpdatedAt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * SYS_ADMIN: Создание нового tenant
     * POST /api/v1/{tenantCode}/admin/tenants
     */
    @PutMapping("/{tid}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<TenantDto> updateTenant(
            @PathVariable String tenantCode,
            @RequestBody TenantDto tenantDto) {
        try {
            
            TenantEntity tenantEntity = new TenantEntity();
              
            tenantEntity.setName(tenantDto.getName());
            tenantEntity.setCode(tenantDto.getCode());

            TenantEntity tenant = adminUserManagementService.updateTenant(tenantEntity);

            TenantDto response = new TenantDto();
            response.setId(tenant.getId());
            response.setName(tenant.getName());
            response.setCode(tenant.getCode());
            response.setIsDeleted(tenant.getDeleted());
            response.setCreatedAt(tenant.getCreatedAt());
            response.setUpdatedAt(tenant.getUpdatedAt());
            return ResponseEntity.ok(response);
            
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     * DELETE /api/v1/{tenantCode}/admin/tenants/{tenantResourceId}
     */
    @DeleteMapping("/{tid}")
    @PreAuthorize("hasRole('SYS_ADMIN')")
    public ResponseEntity<Void> deleteTenant(
            @PathVariable String tenantCode) {
        try {
            adminUserManagementService.deleteTenant(tenantCode);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


}


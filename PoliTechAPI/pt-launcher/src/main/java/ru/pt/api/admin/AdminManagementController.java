package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.model.AdminResponse;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;
import ru.pt.api.dto.exception.BadRequestException;

import java.util.List;


/**
 * Контроллер для управления администраторами всех уровней
 * TNT_ADMIN, GROUP_ADMIN, PRODUCT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/admins
 * tenantCode: pt, vsk, msg
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admins/roles")
public class AdminManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;

    public AdminManagementController(SecurityContextHelper securityContextHelper,
                                    AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.adminUserManagementService = adminUserManagementService;
    }

    // ========== UNIFIED ADMIN MANAGEMENT METHODS ==========

    /**
     * Unified GET endpoint for admins by role name
     * GET /api/v1/{tenantCode}/admin/admins/{role_name}
     * role_name: sys-admin, tnt-admin, product-admin
     */
    @GetMapping("/{roleName}")
    public ResponseEntity<List<AdminResponse>> getAdminsByRole(
            @PathVariable String tenantCode,
            @PathVariable String roleName) {
        
        UserRole role = UserRole.valueOf(roleName.toUpperCase());
        if (role == null) {
            throw new BadRequestException("Invalid role: " + roleName + ". Valid values: SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN");
        }
        List<AdminResponse> admins = adminUserManagementService.getAdmins(tenantCode, role);
        return ResponseEntity.ok(admins);
    }

    /**
     * Unified POST endpoint for creating admins
     * POST /api/v1/{tenantCode}/admin/admins
     * Body: RoleAssignmentRequest with role and login
     */
    @PostMapping
    public ResponseEntity<AdminResponse> createAdmin(
            @PathVariable String tenantCode,
            @RequestBody RoleAssignmentRequest request) {
        
        if (request.role() == null || request.userLogin() == null) {
            throw new BadRequestException("Role and login are required");
        }
        
        UserRole role = UserRole.valueOf(request.role().toUpperCase());
        if (role == null) {
            throw new BadRequestException("Invalid role: " + request.role() + ". Valid values: SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN");
        }
        AdminResponse admin = adminUserManagementService.createAdmin(tenantCode, request.authClientId(), request.userLogin(), role);
        return ResponseEntity.ok(admin);
    }

    /**
     * Unified DELETE endpoint for deleting admins by ID
     * DELETE /api/v1/{tenantCode}/admin/admins/{adminId}
     * The role is determined from the admin record
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteAdmin(
            @PathVariable String tenantCode,
            @PathVariable Long roleId) {
        adminUserManagementService.deleteAdmin(tenantCode, roleId);
        return ResponseEntity.noContent().build();
    }

    public record RoleAssignmentRequest(String role, String userLogin, String authClientId) {}

}

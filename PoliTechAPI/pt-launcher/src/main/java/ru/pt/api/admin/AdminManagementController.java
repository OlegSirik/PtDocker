package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.model.AdminResponse;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.admin.AdminManagementService;
import ru.pt.api.dto.exception.BadRequestException;

import java.util.List;


/**
 * Контроллер для управления администраторами всех уровней
 * TNT_ADMIN, GROUP_ADMIN, PRODUCT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/admins
 * tenantCode: pt, vsk, msg
 * /account/{id}/roles
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;

    public AdminManagementController(SecurityContextHelper securityContextHelper,
                                    AdminManagementService adminManagementService) {
        this.adminManagementService = adminManagementService;
    }

    @GetMapping("/accounts/{accountId}/members")
    public ResponseEntity<List<AdminResponse>> getMembers(
            @PathVariable String tenantCode,
            @PathVariable Long accountId,
            @RequestParam(required = false) String role) {
        List<AdminResponse> members = adminManagementService.getAccountMembers(accountId, role);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/accounts/{accountId}/members")
    public ResponseEntity<AdminResponse> addMember(
            @PathVariable String tenantCode,
            @PathVariable Long accountId,
            @RequestBody AddMemberRequest request) {        
        
        AdminResponse member = adminManagementService.setAccountMember(accountId, request.role(), request.userLogin()); 
        return ResponseEntity.ok(member);
    }

    @DeleteMapping("/accounts/{accountId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable String tenantCode,
            @PathVariable Long accountId,
            @PathVariable Long memberId) {        
        
        adminManagementService.deleteAccountMember(accountId, memberId); 
        return ResponseEntity.noContent().build();
    }

    /* Методы для тенанта и клиента sys */
    @GetMapping("/admins/members")
    public ResponseEntity<List<AdminResponse>> getTenantMembers(
            @PathVariable String tenantCode,
            @RequestParam(required = false) String role) {
        
        Long accountId = adminManagementService.getSysAccountId();
        List<AdminResponse> members = adminManagementService.getAccountMembers(accountId, role == null ? null : UserRole.valueOf(role.toUpperCase()).toString());
        return ResponseEntity.ok(members);
    }

    @PostMapping("/admins/members")
    public ResponseEntity<AdminResponse> addMember(
            @PathVariable String tenantCode,
            @RequestBody AddMemberRequest request) {        
        
        Long accountId = adminManagementService.getSysAccountId();
        AdminResponse member = adminManagementService.setAccountMember(accountId, request.role(), request.userLogin()); 
        return ResponseEntity.ok(member);
    }

    @DeleteMapping("/admins/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable String tenantCode,
            @PathVariable Long memberId) {        
        
        Long accountId = adminManagementService.getSysAccountId();
        adminManagementService.deleteAccountMember(accountId, memberId); 
        return ResponseEntity.noContent().build();
    }

//-------------------------
    //public record RoleAssignmentRequest(String role, String userLogin, String authClientId) {}
    public record AddMemberRequest(String role, String userLogin) {}
    public record AccountUser(Long id, String role, String userLogin, String fullName, String position) {}
}

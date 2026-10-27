package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.model.AdminResponse;
import ru.pt.auth.model.LoginDto;
import ru.pt.auth.service.admin.AdminManagementService;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.auth.service.LoginManagementService;
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
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin")
public class AdminManagementController {

    private final AdminManagementService adminManagementService;
    private final LoginManagementService loginManagementService;

    private boolean notNull(String value) {
        return value != null && !value.isBlank();
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
        ensureLogin(tenantCode, request);
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
        String roleName = null;
        if (role != null && !role.isBlank()) {
            UserRole parsed = UserRole.fromString(role);
            if (parsed == null) {
                throw new BadRequestException("Unknown role: " + role);
            }
            roleName = parsed.getValue();
        }
        List<AdminResponse> members = adminManagementService.getAccountMembers(accountId, roleName);
        return ResponseEntity.ok(members);
    }

    @PostMapping("/admins/members")
    public ResponseEntity<AdminResponse> addTenantAdmin(
            @PathVariable String tenantCode,
            @RequestBody AddMemberRequest request) {
        ensureLogin(tenantCode, request);
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
    public record AddMemberRequest(String role, String userLogin, String fullName, String position, String password) {}

    /**
     * Существующий логин — только привязка. Нет логина — создать (fullName по умолчанию = userLogin).
     */
    private void ensureLogin(String tenantCode, AddMemberRequest request) {
        if (request == null || request.userLogin() == null || request.userLogin().isBlank()) {
            throw new BadRequestException("userLogin is required");
        }
        String userLogin = request.userLogin().trim();
        String effectiveTenant = loginManagementService.resolveLoginTenantCode(tenantCode);
        LoginDto login = loginManagementService.getLoginByUserLogin(effectiveTenant, userLogin);
        if (login != null) {
            return;
        }
        String fullName = notNull(request.fullName()) ? request.fullName().trim() : userLogin;
        loginManagementService.createLogin(effectiveTenant, userLogin, fullName, request.position());
        if (notNull(request.password())) {
            loginManagementService.setPassword(effectiveTenant, userLogin, request.password());
        }
    }
}

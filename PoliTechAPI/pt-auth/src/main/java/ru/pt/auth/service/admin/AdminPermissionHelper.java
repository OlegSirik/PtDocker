package ru.pt.auth.service.admin;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

/**
 * Shared permission checks for admin operations.
 * Used by AdminManagementService, LoginManagementService.
 */
@Component
public class AdminPermissionHelper {

    private final SecurityContextHelper securityContextHelper;

    public AdminPermissionHelper(SecurityContextHelper securityContextHelper) {
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Get current authenticated user.
     * @throws ForbiddenException if not authenticated
     */
    public UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }

    /**
     * Check if current user is SYS_ADMIN.
     */
    public boolean userIsSysAdmin() {
        UserDetailsImpl currentUser = getCurrentUser();
        return UserRole.SYS_ADMIN.getValue().equals(currentUser.getUserRole());
    }

    /**
     * Check if current user is TNT_ADMIN and has access to the given tenant.
     */
    public boolean userIsTntAdmin(String tenantCode) {
        UserDetailsImpl currentUser = getCurrentUser();
        return UserRole.TNT_ADMIN.getValue().equals(currentUser.getUserRole())
                && tenantCode != null
                && tenantCode.equals(currentUser.getTenantCode());
    }

    /**
     * Get tenant code for SYS_ADMIN (impersonated) or current user's tenant.
     * For SYS_ADMIN: returns impersonated tenant from header (required for cross-tenant operations).
     * For others: returns their tenant code.
     */
    public String getUserEffectiveTenantCode() {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!UserRole.SYS_ADMIN.getValue().equals(currentUser.getUserRole())) {
            return currentUser.getTenantCode();
        }
        String impersonated = currentUser.getImpersonatedTenantCode();
        if (impersonated == null) {
            throw new ForbiddenException("SYS ADMIN must be impersonated");
        }
        return impersonated;
    }

    /**
     * Verify that current user (SYS_ADMIN or TNT_ADMIN) can perform admin operations
     * for the given tenant and return the effective tenant code.
     * <p>
     * - TNT_ADMIN: must pass their own tenantCode
     * - SYS_ADMIN: returns impersonated tenant (from header)
     *
     * @param tenantCode tenant code from request (for TNT_ADMIN must match their tenant)
     * @return effective tenant code for the operation
     * @throws ForbiddenException if user lacks permission
     */
    public String checkPermissionAndGetTenantCode(String tenantCode) {
        if (userIsTntAdmin(tenantCode)) {
            return tenantCode;
        }
        if (userIsSysAdmin()) {
            return getUserEffectiveTenantCode();
        }
        throw new ForbiddenException("Only SYS_ADMIN or TNT_ADMIN can access admin operations");
    }

    /**
     * Verify tenant access for login management (broader: allows CLIENT_ADMIN, GROUP_ADMIN for own tenant).
     * - SYS_ADMIN: returns impersonated tenant
     * - Others: tenantCode must match current user's tenant
     */
    public String checkPermissionAndGetTenantCodeForLoginManagement(String tenantCode) {
        if (userIsSysAdmin()) {
            return getUserEffectiveTenantCode();
        }
        UserDetailsImpl currentUser = getCurrentUser();
        if (!tenantCode.equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Only SYS_ADMIN or TNT_ADMIN can access other tenants");
        }
        return tenantCode;
    }
}

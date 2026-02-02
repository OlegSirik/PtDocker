package ru.pt.auth.security.permitions;

import ru.pt.api.security.AuthenticatedUser;

/**
 * Centralized authorization service.
 *
 * Usage:
 *  - called explicitly from application services
 *  - no Spring Security annotations here
 *  - enforces tenant, product and resource-level permissions
 */
public interface AuthorizationService {

    /**
     * Generic authorization check.
     *
     * @param user the authenticated user
     * @param resourceType the type of resource being accessed
     * @param resourceId the ID of the specific resource (can be null for list operations)
     * @param resourceAccountId the account ID that owns the resource
     * @param action the action being performed
     * @throws ForbiddenException if access is denied
     */
    void check(
        AuthenticatedUser user,
        AuthZ.ResourceType resourceType,
        String resourceId,
        Long resourceAccountId,
        AuthZ.Action action);

}

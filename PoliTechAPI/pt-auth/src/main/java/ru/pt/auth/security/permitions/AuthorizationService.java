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

    /**
     * Checks whether the user has permission to perform the given action on the resource type.
     * Does not throw; use {@link #check} when you need to enforce access and throw on denial.
     *
     * @param user the authenticated user
     * @param resourceType the type of resource
     * @param action the action to check
     * @return true if the user has the permission, false otherwise
     */
    boolean userHasPermition(AuthenticatedUser user,
        AuthZ.ResourceType resourceType,
        AuthZ.Action action);

}

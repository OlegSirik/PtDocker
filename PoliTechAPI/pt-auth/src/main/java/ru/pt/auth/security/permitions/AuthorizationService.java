package ru.pt.auth.security.permitions;

import ru.pt.auth.security.UserDetailsImpl;
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
     * @throws ForbiddenException if access is denied
     */
    public void check(
        UserDetailsImpl user,
        AuthZ.ResourceType resourceType,
        String resourceId,
        Long resoureAccountId,
        AuthZ.Action action );

}

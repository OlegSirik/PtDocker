package ru.pt.auth.security.permitions;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AccountProductRoles;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.auth.service.AccountDataService;

/**
 * Authorization service implementation.
 * Uses AccountDataService for data access (no circular dependency).
 */
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private final AccountDataService accountDataService;
    private final AccountProductRoles accountProductRoles;

    @Override
    public void check(
            AuthenticatedUser user,
            AuthZ.ResourceType resourceType,
            String resourceId,
            Long resourceAccountId,
            AuthZ.Action action
    ) {

        if (log.isTraceEnabled()) {
            log.trace("check(userId={}, role={}, resourceType={}, resourceId={}, resourceAccountId={}, action={})",
                    user.getId(), user.getUserRole(), resourceType, resourceId, resourceAccountId, action);
        }

        Long actingAccountId = user.getActingAccountId();

        // Account hierarchy check (SQL)
        if (resourceAccountId != null) {
            if (!accountDataService.isParent(actingAccountId, resourceAccountId)) {
                if (log.isTraceEnabled()) {
                    log.trace("Access denied by hierarchy: actingAccountId={} !> resourceAccountId={}",
                            actingAccountId, resourceAccountId);
                }
                throw new ForbiddenException("Нет доступа к ресурсу");
            }
        }

        // Applicability check
//        if (!AuthZMatrix.isApplicable(resourceType, action)) {
//            throw new IllegalArgumentException(
//                    "Action %s not applicable to %s"
//                            .formatted(action, resourceType)
//            );
//        }

        // Permission check
        // Для продукта проверка что user может выполнять такую операцию. 
        if (!AuthZMatrix.roleHasPermission(AuthZ.Role.valueOf(user.getUserRole()), resourceType, action)) {
            if (log.isTraceEnabled()) {
                log.trace("Access denied by role matrix: role={}, resourceType={}, action={}",
                        user.getUserRole(), resourceType, action);
            }
            throw new ForbiddenException(
                    "Access denied: %s %s %s"
                            .formatted(AuthZ.Role.valueOf(user.getUserRole()),resourceType, action)
            );
        }

        // Проверка для продукта
        // Потом раскоментить
        /* 
        if (resourceType == AuthZ.ResourceType.PRODUCT && resourceId != null && !resourceId.isEmpty()) {
            if (!checkProductAction( Long.getLong(resourceId), actingAccountId, action)) {
                throw new ForbiddenException(
                    "Access denied: %s %s %s"
                            .formatted(resourceType, resourceId, action)
            );
            } 
        }
            */
        
    }

    public boolean userHasPermition(AuthenticatedUser user,
        AuthZ.ResourceType resourceType,
        AuthZ.Action action) {
        boolean result = AuthZMatrix.roleHasPermission(AuthZ.Role.valueOf(user.getUserRole()), resourceType, action);
        if (log.isTraceEnabled()) {
            log.trace("userHasPermition(userId={}, role={}, resourceType={}, action={}) -> {}",
                    user.getId(), user.getUserRole(), resourceType, action, result);
        }
        return result;
    }


    public boolean checkProductAction(
            AuthenticatedUser user,
            Long productId,
            AuthZ.Action action
    ) {

        Long actingAccountId = user.getActingAccountId();

        ProductRole productRole = accountProductRoles.getProductRole(actingAccountId, productId);
        if (productRole == null) {
            if (log.isTraceEnabled()) {
                log.trace("checkProductAction: no ProductRole found for actingAccountId={}, productId={}",
                        actingAccountId, productId);
            }
            throw new ForbiddenException(
                "Нет прав на действие %s для продукта %s"
                        .formatted(action, productId));
        }
        boolean result;
        if (action == AuthZ.Action.VIEW) { result = productRole.canRead(); }
        else if (action == AuthZ.Action.QUOTE) { result = productRole.canQuote(); }
        else if (action == AuthZ.Action.SELL) { result = productRole.canPolicy(); }
        else { result = false; }

        if (log.isTraceEnabled()) {
            log.trace("checkProductAction(userId={}, actingAccountId={}, productId={}, action={}) -> {}",
                    user.getId(), actingAccountId, productId, action, result);
        }

        return result;
       
    }

}
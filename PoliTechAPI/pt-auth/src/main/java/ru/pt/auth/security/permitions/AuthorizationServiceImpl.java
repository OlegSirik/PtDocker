package ru.pt.auth.security.permitions;

import lombok.RequiredArgsConstructor;
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
        
        Long actingAccountId = user.getActingAccountId();

        // Account hierarchy check (SQL)
        if (resourceAccountId != null) {
            if (!accountDataService.isParent(actingAccountId, resourceAccountId)) {
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
            throw new ForbiddenException(
                    "Access denied: %s %s %s"
                            .formatted(resourceType, resourceId, action)
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
        return AuthZMatrix.roleHasPermission(AuthZ.Role.valueOf(user.getUserRole()), resourceType, action);
    }


        /* 
     * Check product access permissions
     * create, quote, save,
     * 
     * @param user current user
     * @param resourceType resource type (PRODUCT)
     * @param productId product ID
     * @param resourceAccountId account ID NA
     * @param action action to check
     * @return list of product role data from hierarchy
     */
    public boolean checkProductAction(
            Long productId,
            Long accountId,
            AuthZ.Action action
    ) {

        
        ProductRole productRole = accountProductRoles.getProductRole(accountId, productId);
        if (productRole == null) {
            throw new ForbiddenException(
                "Нет прав на действие %s для продукта %s"
                        .formatted(action, productId));
        }
        if (action == AuthZ.Action.VIEW) { return productRole.canRead(); }
        if (action == AuthZ.Action.QUOTE) { return productRole.canQuote(); }
        if (action == AuthZ.Action.SELL) { return productRole.canPolicy(); }

        return false;
        /*
        record ProductRole(
    Long id,
    Long tid,
    Long clientId,
    Long accountId,
    Long roleProductId,
    String roleProductName,
    Long roleAccountId,
    Boolean isDeleted,
    Boolean canRead,
    Boolean canQuote,
    Boolean canPolicy,
    Boolean canAddendum,
    Boolean canCancel,
    Boolean canProlongate
     */
    }

}
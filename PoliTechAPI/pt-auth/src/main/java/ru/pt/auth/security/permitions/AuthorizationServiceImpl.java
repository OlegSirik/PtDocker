package ru.pt.auth.security.permitions;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.AccountDataService;

/**
 * Authorization service implementation.
 * Uses AccountDataService for data access (no circular dependency).
 */
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final AccountDataService accountDataService;

    public void check(
            UserDetailsImpl user,
            AuthZ.ResourceType resourceType,
            String resourceId,
            Long resoureAccountId,
            AuthZ.Action action
    ) {
        
        Long myAccountId = user.getAccountId();
        Long actingAccountId = user.getActingAccountId();
        String myRoleName = user.getUserRole();

        // Account hierarchy check (SQL)
        if (resoureAccountId != null) {
            if (!accountDataService.isParent(actingAccountId, resoureAccountId)) {
                throw new ForbiddenException("Нет доступа к ресурсу");
            }
        }

        // Applicability check
        if (!AuthZ.isApplicable(resourceType, action)) {
            throw new IllegalArgumentException(
                    "Action %s not applicable to %s"
                            .formatted(action, resourceType)
            );
        }

        // Permission check
        if (!AuthZ.roleHasPermission(AuthZ.Role.valueOf(user.getUserRole()), resourceType, action)) {
            throw new ForbiddenException(
                    "Access denied: %s %s %s"
                            .formatted(resourceType, resourceId, action)
            );
        }
        
    }

    /**
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
    public void checkProduct(
            Long productId,
            Long accountId,
            AuthZ.Action action
    ) {

        // UserDetailsImpl | AuthZ.ResourceType | resourceId  | resourceAccountId | AuthZ.Action action
        // quote           | Policy             | product_id  | ?                 | QUOTE
        // save            | Policy             | product_id  | ?                 | CREATE   

        // view            | Policy             | product_id  | policy.account_id | VIEW    Начиная от policy.account_id идем вверх и проверяем что там есть actingAccountId
        // addendum        |
        // prolongate      |


        ProductRole productRole = accountDataService.getProductRole(accountId, productId);

        if (productRole == null) {
            throw new ForbiddenException(
                "Нет прав на действие %s для продукта %s"
                        .formatted(action, productId));
        }

        // Check specific product action permissions
        
    }

}
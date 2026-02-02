package ru.pt.api.service.auth;

import ru.pt.api.dto.auth.ProductRole;
import java.util.List;

/**
 * Interface for product role data access.
 * Used by AuthorizationService for permission checks.
 * No authorization - pure data access.
 */
public interface AccountProductRoles {
    
    /**
     * Get product role for a specific account and product.
     * Searches up the account hierarchy for inherited permissions.
     * @param accountId account ID
     * @param productId product ID
     * @return product role if found, null otherwise
     */
    ProductRole getProductRole(Long accountId, Long productId);

    /**
     * Get all product roles for an account (raw data access).
     * @param accountId account ID
     * @return list of product roles
     */
    List<ProductRole> getProductRolesByAccountIdRaw(Long accountId);

}

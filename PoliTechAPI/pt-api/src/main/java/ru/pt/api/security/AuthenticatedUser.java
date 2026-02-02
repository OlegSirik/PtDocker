package ru.pt.api.security;

import java.util.Set;

/**
 * Interface representing an authenticated user's context.
 * Used across all modules without dependency on pt-auth implementation.
 * 
 * Principal (accountId) — WHO
 * Acting account — WHERE (root of access tree)
 * Permission — WHAT
 * Resource — ON WHAT
 */
public interface AuthenticatedUser {
    
    /**
     * Get the unique identifier of the user's account login record
     */
    Long getId();
    
    /**
     * Get the username (login)
     */
    String getUsername();
    
    // ===== Tenant context =====
    
    /**
     * Get the tenant code (e.g., "VSK", "PT")
     */
    String getTenantCode();
    
    /**
     * Get the tenant ID
     */
    Long getTenantId();
    
    // ===== Account context =====
    
    /**
     * Get the account ID (leaf node in hierarchy)
     */
    Long getAccountId();
    
    /**
     * Get the account name
     */
    String getAccountName();
    
    /**
     * Get the acting account ID (root of access tree, may differ from accountId)
     */
    Long getActingAccountId();
    
    // ===== Client context =====
    
    /**
     * Get the client ID
     */
    Long getClientId();
    
    /**
     * Get the client name
     */
    String getClientName();
    
    // ===== Roles & permissions =====
    
    /**
     * Get the user's primary role (e.g., "CLIENT_ADMIN", "GROUP_ADMIN")
     */
    String getUserRole();
    
    /**
     * Get all product roles assigned to the user
     */
    Set<String> getProductRoles();
    
    /**
     * Check if user is the default account for this login
     */
    boolean isDefault();
    
    /**
     * Check if user has a specific product role
     * @param productRole the product role to check
     * @return true if user has the role
     */
    boolean hasProductRole(String productRole);
    
    /**
     * Check if user can perform an action on a product
     * @param productCode the product code
     * @param action the action (e.g., "READ", "QUOTE", "POLICY")
     * @return true if user can perform the action
     */
    boolean canPerformAction(String productCode, String action);
}

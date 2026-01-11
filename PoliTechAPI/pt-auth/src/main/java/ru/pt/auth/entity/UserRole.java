package ru.pt.auth.entity;

/**
 * Enumeration of user roles in the system.
 * These roles define the administrative and functional permissions for users.
 */
public enum UserRole {
    
    /**
     * System Administrator - Platform admin who can create new tenants.
     * Highest level of access.
     */
    SYS_ADMIN("SYS_ADMIN"),
    
    /**
     * Tenant Administrator - Admin of a tenant section.
     * Can create client accounts and manage tenant resources.
     */
    TNT_ADMIN("TNT_ADMIN"),
    
    /**
     * Client Administrator - Admin of a client.
     * Can create accounts and manage client resources.
     */
    CLIENT_ADMIN("CLIENT_ADMIN"),
    /**
     * Group Administrator - Admin of a group, product manager, etc.
     * Can manage accounts and permissions within their groups.
     */
    GROUP_ADMIN("GROUP_ADMIN"),
    
    /**
     * Product Administrator - Product manager.
     * Manages product constructor.
     */
    PRODUCT_ADMIN("PRODUCT_ADMIN"),
    
    /**
     * Sales - Sales representatives.
     * Can view their contracts and perform actions within their permissions.
     */
    SALES("SALES"),
    
    /**
     * Default User - Default user for client accounts.
     */
    DEFAULT_USER("DEFAULT_USER");
    
    private final String value;
    
    UserRole(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Converts a string value to UserRole enum.
     * @param value the string role value
     * @return the corresponding UserRole enum, or null if not found
     */
    public static UserRole fromString(String value) {
        if (value == null) {
            return null;
        }
        for (UserRole role : UserRole.values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }
    
    /**
     * Checks if the given string value is a valid role.
     * @param value the string role value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        return fromString(value) != null;
    }
    
    @Override
    public String toString() {
        return value;
    }
}


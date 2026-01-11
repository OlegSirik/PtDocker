package ru.pt.auth.entity;

/**
 * Enumeration of account node types in the system.
 * Represents the hierarchical structure of accounts: TENANT -> CLIENT -> GROUP -> ACCOUNT -> SUB
 * Also includes administrative account types.
 */
public enum AccountNodeType {
    
    /**
     * Tenant level account - root of the hierarchy
     */
    TENANT("TENANT"),
    
    /**
     * Client level account
     */
    CLIENT("CLIENT"),
    
    /**
     * Group level account
     */
    GROUP("GROUP"),
    
    /**
     * Account level - regular account
     */
    ACCOUNT("ACCOUNT"),
    
    /**
     * Sub-account level - sub-account of an account
     */
    SUB("SUB"),
    
    /**
     * System Administrator account
     */
    SYS_ADMIN("SYS_ADMIN"),
    
    /**
     * Tenant Administrator account
     */
    TNT_ADMIN("TNT_ADMIN"),
    
    /**
     * Client Administrator account
     */
    CLIENT_ADMIN("CLIENT_ADMIN"),
    
    /**
     * Group Administrator account
     */
    GROUP_ADMIN("GROUP_ADMIN"),
    
    /**
     * Product Administrator account
     */
    PRODUCT_ADMIN("PRODUCT_ADMIN");
    
    private final String value;
    
    AccountNodeType(String value) {
        this.value = value;
    }
    
    /**
     * Gets the string value of the enum.
     * @return the string representation
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Converts a string value to AccountNodeType enum.
     * @param value the string node type value
     * @return the corresponding AccountNodeType enum, or null if not found
     */
    public static AccountNodeType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (AccountNodeType type : AccountNodeType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Checks if the given string value is a valid node type.
     * @param value the string node type value to check
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

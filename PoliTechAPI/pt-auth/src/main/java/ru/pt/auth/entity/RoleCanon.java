package ru.pt.auth.entity;

import ru.pt.api.service.auth.AuthZ;

/**
 * Единый канон ролей: AccountNodeType / UserRole / AuthZ.Role.
 * Строки: SYS_ADMIN, TNT_ADMIN, PRODUCT_ADMIN, GROUP_ADMIN.
 * CLIENT_ADMIN считается GROUP_ADMIN. SALES / DEFAULT_USER мапятся в ACCOUNT.
 */
public final class RoleCanon {

    private RoleCanon() {
    }

    public static AuthZ.Role toAuthZ(AccountNodeType nodeType) {
        if (nodeType == null) {
            return null;
        }
        return switch (nodeType) {
            case SYS_ADMIN -> AuthZ.Role.SYS_ADMIN;
            case TNT_ADMIN -> AuthZ.Role.TNT_ADMIN;
            case PRODUCT_ADMIN -> AuthZ.Role.PRODUCT_ADMIN;
            case GROUP_ADMIN, CLIENT_ADMIN -> AuthZ.Role.GROUP_ADMIN;
            case ACCOUNT -> AuthZ.Role.ACCOUNT;
            case SUB -> AuthZ.Role.SUB;
            default -> null;
        };
    }

    public static AuthZ.Role toAuthZ(UserRole role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case SYS_ADMIN -> AuthZ.Role.SYS_ADMIN;
            case TNT_ADMIN -> AuthZ.Role.TNT_ADMIN;
            case PRODUCT_ADMIN -> AuthZ.Role.PRODUCT_ADMIN;
            case GROUP_ADMIN, CLIENT_ADMIN -> AuthZ.Role.GROUP_ADMIN;
            case SALES, DEFAULT_USER -> AuthZ.Role.ACCOUNT;
        };
    }

    public static boolean isAssignableAdminRole(AccountNodeType nodeType) {
        return nodeType == AccountNodeType.SYS_ADMIN
                || nodeType == AccountNodeType.TNT_ADMIN
                || nodeType == AccountNodeType.PRODUCT_ADMIN
                || nodeType == AccountNodeType.GROUP_ADMIN
                || nodeType == AccountNodeType.CLIENT_ADMIN;
    }

    public static boolean isAssignableAdminRole(UserRole role) {
        return role == UserRole.SYS_ADMIN
                || role == UserRole.TNT_ADMIN
                || role == UserRole.PRODUCT_ADMIN
                || role == UserRole.GROUP_ADMIN
                || role == UserRole.CLIENT_ADMIN;
    }

    public static AuthZ.Role toAuthZ(String raw) {
        AuthZ.Role parsed = AuthZ.Role.parse(raw);
        if (parsed != null) {
            return parsed;
        }
        AccountNodeType nodeType = AccountNodeType.fromString(raw);
        if (nodeType != null) {
            AuthZ.Role fromNode = toAuthZ(nodeType);
            if (fromNode != null) {
                return fromNode;
            }
        }
        return toAuthZ(UserRole.fromString(raw));
    }
}

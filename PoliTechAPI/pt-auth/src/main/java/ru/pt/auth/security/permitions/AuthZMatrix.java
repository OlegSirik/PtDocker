package ru.pt.auth.security.permitions;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

import ru.pt.api.service.auth.AuthZ;

/**
 * Matrix of applicable actions per resource type.
 *
 * Используется в AuthorizationServiceImpl для проверки, что действие
 * корректно для типа ресурса, независимо от конкретных прав пользователя.
 */
public final class AuthZMatrix {

    /** Матрица соответствия: ResourceType -> Set<Action> */
    private static final EnumMap<AuthZ.ResourceType, Set<AuthZ.Action>> MATRIX =
        new EnumMap<>(AuthZ.ResourceType.class);

    /** Mapping: Role -> Set<permissions (ResourceType:Action)> */
    private static final EnumMap<AuthZ.Role, Set<String>> ROLE_PERMISSIONS =
        new EnumMap<>(AuthZ.Role.class);

    static {
        // ======================
        // Action applicability
        // ======================
        MATRIX.put(AuthZ.ResourceType.TENANT, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.CLIENT, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.CLIENT_PRODUCTS, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.TENANT_ADMIN, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));

        MATRIX.put(AuthZ.ResourceType.LOB, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));

        MATRIX.put(AuthZ.ResourceType.PRODUCT, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));

        MATRIX.put(AuthZ.ResourceType.ACCOUNT, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.ACCOUNT_PRODUCT, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));

        MATRIX.put(AuthZ.ResourceType.TOKEN, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.LOGIN, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.CONTRACT, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));
        MATRIX.put(AuthZ.ResourceType.POLICY_ADDON, EnumSet.of(AuthZ.Action.MANAGE, AuthZ.Action.LIST, AuthZ.Action.VIEW));

        // ======================
        // Role -> permissions
        // ======================
        // пример: ADMIN может всё
        ROLE_PERMISSIONS.put(AuthZ.Role.SYS_ADMIN, Set.of(
            formatPermission(AuthZ.ResourceType.TENANT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.TENANT_ADMIN, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CLIENT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CLIENT_PRODUCTS, AuthZ.Action.VIEW)
        ));
        ROLE_PERMISSIONS.put(AuthZ.Role.TNT_ADMIN, Set.of(
            formatPermission(AuthZ.ResourceType.TENANT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.TENANT_ADMIN, AuthZ.Action.ALL),

            formatPermission(AuthZ.ResourceType.CLIENT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CLIENT_PRODUCTS, AuthZ.Action.ALL),

            formatPermission(AuthZ.ResourceType.LOB, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.PRODUCT, AuthZ.Action.ALL),

            // ToDo - delete it just to test
            formatPermission(AuthZ.ResourceType.ACCOUNT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.ACCOUNT_PRODUCT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.TOKEN, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.LOGIN, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CONTRACT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.POLICY_ADDON, AuthZ.Action.ALL),

            formatPermission(AuthZ.ResourceType.POLICY, AuthZ.Action.TEST)
        ));
        // PRODUCT_ADMIN может SELL и VIEW продуктов
        ROLE_PERMISSIONS.put(AuthZ.Role.PRODUCT_ADMIN, Set.of(
            formatPermission(AuthZ.ResourceType.TENANT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.TENANT_ADMIN, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.PRODUCT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CLIENT_PRODUCTS, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CLIENT, AuthZ.Action.LIST),
            formatPermission(AuthZ.ResourceType.CLIENT, AuthZ.Action.VIEW),
            // ToDo - delete it just to test
            formatPermission(AuthZ.ResourceType.ACCOUNT, AuthZ.Action.MANAGE),
            formatPermission(AuthZ.ResourceType.ACCOUNT_PRODUCT, AuthZ.Action.ALL),
            // formatPermission(AuthZ.ResourceType.ACCOUNT, AuthZ.Action.PERMISSION),
            formatPermission(AuthZ.ResourceType.LOB, AuthZ.Action.ALL),

            formatPermission(AuthZ.ResourceType.TOKEN, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.LOGIN, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.CONTRACT, AuthZ.Action.ALL),
            formatPermission(AuthZ.ResourceType.POLICY_ADDON, AuthZ.Action.ALL),

            formatPermission(AuthZ.ResourceType.POLICY, AuthZ.Action.TEST)
        ));

        ROLE_PERMISSIONS.put(AuthZ.Role.ACCOUNT, Set.of(
            formatPermission(AuthZ.ResourceType.POLICY, AuthZ.Action.SELL),
            formatPermission(AuthZ.ResourceType.POLICY, AuthZ.Action.QUOTE)
        ));

        // AGENT — только VIEW продукта и POLICY
    }

    private AuthZMatrix() {
        // нельзя инстанцировать
    }

    /**
     * Проверяет, применимо ли действие к типу ресурса.
     */
    public static boolean isApplicable(AuthZ.ResourceType resource, AuthZ.Action action) {
        return MATRIX.getOrDefault(resource, Set.of()).contains(action);
    }

    /**
     * Проверяет, есть ли у роли разрешение на ресурс + действие
     */
    public static boolean roleHasPermission(AuthZ.Role role, AuthZ.ResourceType resource, AuthZ.Action action) {
        Set<String> permissions = ROLE_PERMISSIONS.getOrDefault(role, Set.of());
        return permissions.contains(formatPermission(resource, AuthZ.Action.ALL))
            || permissions.contains(formatPermission(resource, action));
    }

    /**
     * Получить все permissions для роли
     */
    public static Set<String> getPermissions(AuthZ.Role role) {
        return Collections.unmodifiableSet(ROLE_PERMISSIONS.getOrDefault(role, Set.of()));
    }

    private static String formatPermission(AuthZ.ResourceType resource, AuthZ.Action action) {
        return resource + ":" + action;
    }
}

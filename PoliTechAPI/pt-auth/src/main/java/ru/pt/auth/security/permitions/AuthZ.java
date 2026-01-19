package ru.pt.auth.security.permitions;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Matrix of applicable actions per resource type.
 * 
 * Используется в AuthorizationServiceImpl для проверки, что действие
 * корректно для типа ресурса, независимо от конкретных прав пользователя.
 */
public final class AuthZ {

    /** Типы ресурсов */
    public enum ResourceType {
        TENANT,
        CLIENT,
        CLIENT_PRODUCTS,
        TENANT_ADMIN,
        LOB,
        PRODUCT
    }

    /** Действия */
    public enum Action {
        ALL, // любые дефствия с объектом
        VIEW,
        CREATE,
        UPDATE,
        DELETE,
        SELL,
        ISSUE,
        CANCEL,
        PAY,
        ASSIGN,
        CLOSE,
        PRINT
    }

    /** Роли пользователей */
    public enum Role {
        SYS_ADMIN("SYS_ADMIN"),
        TNT_ADMIN("TNT_ADMIN"),
        GROUP_ADMIN("GROUP_ADMIN"),
        PRODUCT_ADMIN("PRODUCT_ADMIN"),
        SALE("SALE");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /** Матрица соответствия: ResourceType -> Set<Action> */
    private static final EnumMap<ResourceType, Set<Action>> MATRIX =
            new EnumMap<>(ResourceType.class);

    /** Mapping: Role -> Set<permissions (ResourceType:Action)> */
    private static final EnumMap<Role, Set<String>> ROLE_PERMISSIONS =
            new EnumMap<>(Role.class);

    static {
        // ======================
        // Action applicability
        // ======================
        MATRIX.put(ResourceType.TENANT, EnumSet.of(Action.ALL));
        MATRIX.put(ResourceType.CLIENT, EnumSet.of(Action.ALL));
        MATRIX.put(ResourceType.CLIENT_PRODUCTS, EnumSet.of(Action.ALL));
        MATRIX.put(ResourceType.TENANT_ADMIN, EnumSet.of(Action.ALL));

        MATRIX.put(ResourceType.LOB, EnumSet.of(Action.VIEW, Action.ALL));
        MATRIX.put(ResourceType.PRODUCT, EnumSet.of(Action.VIEW, Action.ALL));

        // ======================
        // Role -> permissions
        // ======================
        // пример: ADMIN может всё
        ROLE_PERMISSIONS.put(Role.SYS_ADMIN, Set.of(
            formatPermission(ResourceType.TENANT, Action.ALL),
            formatPermission(ResourceType.TENANT_ADMIN, Action.ALL),
            formatPermission(ResourceType.CLIENT, Action.VIEW),
            formatPermission(ResourceType.CLIENT_PRODUCTS, Action.VIEW)
    ));

        // PRODUCT_ADMIN может SELL и VIEW продуктов
        ROLE_PERMISSIONS.put(Role.PRODUCT_ADMIN, Set.of(
                formatPermission(ResourceType.TENANT, Action.ALL),
                formatPermission(ResourceType.TENANT_ADMIN, Action.ALL),
                formatPermission(ResourceType.PRODUCT, Action.SELL),
                formatPermission(ResourceType.CLIENT_PRODUCTS, Action.VIEW),
                formatPermission(ResourceType.CLIENT_PRODUCTS, Action.UPDATE)
        ));

        // AGENT — только VIEW продукта и POLICY
    }

    private AuthZ() {
        // нельзя инстанцировать
    }

    // ======================
    // Public methods
    // ======================

    /**
     * Проверяет, применимо ли действие к типу ресурса.
     */
    public static boolean isApplicable(ResourceType resource, Action action) {
        return MATRIX.getOrDefault(resource, Set.of()).contains(action);
    }

    /**
     * Проверяет, есть ли у роли разрешение на ресурс + действие
     */
    public static boolean roleHasPermission(Role role, ResourceType resource, Action action) {
        Set<String> permissions = ROLE_PERMISSIONS.getOrDefault(role, Set.of());
        return permissions.contains(formatPermission(resource, Action.ALL)) || permissions.contains(formatPermission(resource, action));
    }

    /**
     * Получить все permissions для роли
     */
    public static Set<String> getPermissions(Role role) {
        return Collections.unmodifiableSet(ROLE_PERMISSIONS.getOrDefault(role, Set.of()));
    }

    // ======================
    // Internal helpers
    // ======================
    private static String formatPermission(ResourceType resource, Action action) {
        return resource + ":" + action;
    }
}

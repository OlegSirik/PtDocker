package ru.pt.api.service.auth;

import java.util.Locale;

/**
 * Authorization enums shared across services.
 */
public final class AuthZ {

    /** Типы ресурсов */
    public enum ResourceType {
        TENANT, 
        CLIENT,
        CLIENT_PRODUCTS,
        TENANT_ADMIN,
        LOB,
        PRODUCT,
        ACCOUNT,
        ACCOUNT_PRODUCT,
        TOKEN,
        LOGIN,
        POLICY,
        POLICY_ADDON,
        CONTRACT,
        FILE,
        /** Страховые компании (справочник) */
        INS_COMPANY
    }

    /** Действия */
    public enum Action {
        ALL, // любые дефствия с объектом
        VIEW, // просматривать весь объект
        LIST, // видеть краткий список всех объектов
        MANAGE, // = CRUD

        GO2PROD,  // только для договора - вывод версии в прод
        CREATE, // создание новой записи
        // Для договора
        QUOTE,
        SELL,
        TEST, // если есть этот Action то расчет по версии в статусе DEV иначе только прод PROD
/*         
        
        UPDATE,
        DELETE,
        PERMISSION,  // изменять права  
        QUOTE,
        ISSUE,
        CANCEL,
        PAY,
        ASSIGN,
        CLOSE,
        PRINT
*/
        }

    /** Роли пользователей */
    public enum Role {
        SYS_ADMIN("SYS_ADMIN"),
        TNT_ADMIN("TNT_ADMIN"),
        GROUP_ADMIN("GROUP_ADMIN"),
        PRODUCT_ADMIN("PRODUCT_ADMIN"),
        ACCOUNT("ACCOUNT"),
        SUB("SUB");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        /**
         * Канон строк: {@code SYS_ADMIN}, {@code TNT_ADMIN}, {@code PRODUCT_ADMIN}, {@code GROUP_ADMIN}.
         * Регистр не важен. Неизвестное значение — {@code null}, не exception.
         */
        public static Role parse(String raw) {
            String n = normalize(raw);
            if (n == null) {
                return null;
            }
            for (Role role : values()) {
                if (role.value.equals(n) || role.name().equals(n)) {
                    return role;
                }
            }
            return null;
        }

        public static Role require(String raw) {
            Role role = parse(raw);
            if (role == null) {
                throw new IllegalArgumentException("Unknown role: " + raw);
            }
            return role;
        }
    }

    /** Единый канон имени роли / nodeType. */
    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private AuthZ() {
        // нельзя инстанцировать
    }
}

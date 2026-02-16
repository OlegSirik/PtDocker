package ru.pt.api.service.auth;

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
        CONTRACT
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
    }

    private AuthZ() {
        // нельзя инстанцировать
    }
}

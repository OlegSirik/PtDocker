package ru.pt.api.security;

import ru.pt.api.ApiExceptionHandler;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.exception.UnauthorizedException;

/**
 * Базовый класс для контроллеров с проверкой прав доступа.
 * Предоставляет утилиты для проверки ролей и прав на операции с продуктами.
 */
public abstract class SecuredController extends ApiExceptionHandler {

    /**
     * Проверяет, имеет ли пользователь роль ADMIN
     */
    public void requireAdmin(UserDetailsImpl user) {
        if (user == null || !"SYS_ADMIN".equals(user.getUserRole())) {
            throw new UnauthorizedException("Admin role required");
        }
    }
    /**
     * Проверяет, имеет ли пользователь нужную роль
     */
    protected void requireRole(UserDetailsImpl user, String role) {
        if (user == null || !role.equals(user.getUserRole())) {
            throw new UnauthorizedException(role + " required");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на чтение продукта
     */
    protected void requireProductRead(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "READ")) {
            throw new UnauthorizedException("No read access to product: " + productCode);
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на создание расчётов
     */
    protected void requireProductQuote(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "QUOTE")) {
            throw new UnauthorizedException("No quote access to product: " + productCode);
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на создание полисов
     */
    protected void requireProductPolicy(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "POLICY")) {
            throw new UnauthorizedException("No policy access to product: " + productCode);
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на изменения продукта
     */
    protected void requireProductWrite(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "ADDENDUM")) {
            throw new UnauthorizedException("No write access to product: " + productCode);
        }
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    protected void requireAuthenticated(UserDetailsImpl user) {
        if (user == null) {
            throw new UnauthorizedException("Authentication required");
        }
    }

    /**
     * Получает ID аккаунта текущего пользователя
     */
    protected Long getAccountId(UserDetailsImpl user) {
        requireAuthenticated(user);
        return user.getAccountId();
    }

    /**
     * Получает ID клиента текущего пользователя
     */
    protected Long getClientId(UserDetailsImpl user) {
        requireAuthenticated(user);
        return user.getClientId();
    }

    /**
     * Получает ID тенанта текущего пользователя
     */
    protected Long getTenantId(UserDetailsImpl user) {
        requireAuthenticated(user);
        return user.getTenantId();
    }
}


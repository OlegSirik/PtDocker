package ru.pt.api.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import ru.pt.auth.security.UserDetailsImpl;

/**
 * Базовый класс для контроллеров с проверкой прав доступа.
 * Предоставляет утилиты для проверки ролей и прав на операции с продуктами.
 */
public abstract class SecuredController {

    /**
     * Проверяет, имеет ли пользователь роль ADMIN
     */
    protected void requireAdmin(UserDetailsImpl user) {
        if (user == null || !"ADMIN".equals(user.getUserRole())) {
            throw new AccessDeniedException("Admin role required");
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на чтение продукта
     */
    protected void requireProductRead(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "READ")) {
            throw new AccessDeniedException("No read access to product: " + productCode);
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на создание расчётов
     */
    protected void requireProductQuote(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "QUOTE")) {
            throw new AccessDeniedException("No quote access to product: " + productCode);
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на создание полисов
     */
    protected void requireProductPolicy(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "POLICY")) {
            throw new AccessDeniedException("No policy access to product: " + productCode);
        }
    }

    /**
     * Проверяет, имеет ли пользователь право на изменения продукта
     */
    protected void requireProductWrite(UserDetailsImpl user, String productCode) {
        if (user == null || !user.canPerformAction(productCode, "ADDENDUM")) {
            throw new AccessDeniedException("No write access to product: " + productCode);
        }
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    protected void requireAuthenticated(UserDetailsImpl user) {
        if (user == null) {
            throw new AccessDeniedException("Authentication required");
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


package ru.pt.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Утилита для получения текущего аутентифицированного пользователя.
 */
@Component
public class SecurityContextHelper {

    /**
     * Получает текущего аутентифицированного пользователя
     */
    public Optional<UserDetailsImpl> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            return Optional.of((UserDetailsImpl) principal);
        }

        return Optional.empty();
    }

    /**
     * Получает ID текущего пользователя
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(UserDetailsImpl::getId);
    }

    /**
     * Получает логин текущего пользователя
     */
    public Optional<String> getCurrentUsername() {
        return getCurrentUser().map(UserDetailsImpl::getUsername);
    }

    /**
     * Получает ID текущего аккаунта
     */
    public Optional<Long> getCurrentAccountId() {
        return getCurrentUser().map(UserDetailsImpl::getAccountId);
    }

    /**
     * Получает ID текущего клиента
     */
    public Optional<Long> getCurrentClientId() {
        return getCurrentUser().map(UserDetailsImpl::getClientId);
    }

    /**
     * Получает Code текущего тенанта
     */
    public Optional<String> getCurrentTenantCode() {
        return getCurrentUser().map(UserDetailsImpl::getTenantCode);
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String);
    }

    /**
     * Проверяет, имеет ли пользователь определенную роль продукта
     */
    public boolean hasProductRole(String productRole) {
        return getCurrentUser()
                .map(user -> user.hasProductRole(productRole))
                .orElse(false);
    }

    /**
     * Проверяет, может ли пользователь выполнять операцию над продуктом
     */
    public boolean canPerformAction(String productCode, String action) {
        return getCurrentUser()
                .map(user -> user.canPerformAction(productCode, action))
                .orElse(false);
    }
}


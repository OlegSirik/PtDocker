package ru.pt.api.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.handler.ApiExceptionHandler;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовый класс для контроллеров с проверкой прав доступа.
 * Предоставляет утилиты для проверки ролей и прав на операции с продуктами.
 */
public abstract class SecuredController extends ApiExceptionHandler {

    protected final SecurityContextHelper securityContextHelper;

    protected SecuredController(SecurityContextHelper securityContextHelper) {
        this.securityContextHelper = securityContextHelper;
    }

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
     * Получить текущего аутентифицированного пользователя
     */
    protected UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("User not authenticated"));
    }

    /**
            * Проверка одной из ролей
     */
    protected void requireAnyRole(String... roles) {
        UserDetailsImpl user = getCurrentUser();
        String userRole = user.getUserRole();

        for (String role : roles) {
            if (role.equals(userRole)) {
                return;
            }
        }

        throw new ForbiddenException("Required role: " + String.join(" or ", roles));
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
        // TODO UNCOMMENT AFTER TESTING
//        if (user == null || !user.hasProductRole("QUOTE")) {
//            throw new UnauthorizedException("No quote access to product: " + productCode);
//        }
    }

    /**
     * Проверяет, имеет ли пользователь право на создание полисов
     */
    protected void requireProductPolicy(UserDetailsImpl user, String productCode) {
        if (user == null || !user.hasProductRole("POLICY")) {
            throw new UnauthorizedException("No policy access: " + productCode);
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
     * Получает Code тенанта текущего пользователя
     */
    protected String getTenantCode(UserDetailsImpl user) {
        requireAuthenticated(user);
        return user.getTenantCode();
    }

    /**
     * Построить ответ с ошибкой
     */
    protected ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        response.put("status", status.value());
        return ResponseEntity.status(status).body(response);
    }


    /**
     * Построить успешный ответ
     */
    protected ResponseEntity<Map<String, Object>> buildSuccessResponse(Map<String, Object> data, String message) {
        data.put("message", message);
        return ResponseEntity.ok(data);
    }

    /**
     * Построить успешный ответ для создания
     */
    protected ResponseEntity<Map<String, Object>> buildCreatedResponse(Map<String, Object> data, String message) {
        data.put("message", message);
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    /**
     * Построить простой ответ с сообщением
     */
    protected Map<String, Object> buildSimpleResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return response;
    }

    /**
     * Обработать исключения и вернуть соответствующий ответ
     */
    protected ResponseEntity<Map<String, Object>> handleException(Exception e) {
        if (e instanceof ForbiddenException) {
            return buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } else if (e instanceof BadRequestException) {
            return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (e instanceof NotFoundException) {
            return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
        } else {
            return buildErrorResponse("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


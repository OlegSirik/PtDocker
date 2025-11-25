package ru.pt.api.dto.exception;

/**
 * Исключение для ошибок доступа (403)
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}


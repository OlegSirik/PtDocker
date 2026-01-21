package ru.pt.api.dto.exception;

import ru.pt.api.dto.errors.ErrorModel;

/**
 * Исключение для ошибок доступа (403)
 */
public class ForbiddenException extends RuntimeException {
    private final ErrorModel errorModel;

    public ForbiddenException(ErrorModel errorModel) {
        super(errorModel.getMessage());
        this.errorModel = errorModel;
    }

    public ForbiddenException(String message) {
        super(message);
        this.errorModel = new ErrorModel(403, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
        this.errorModel = new ErrorModel(403, message);
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }
}


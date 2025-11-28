package ru.pt.api.dto.exception;

import ru.pt.api.dto.errors.ErrorModel;

public class UnauthorizedException extends RuntimeException {
    private final ErrorModel errorModel;

    public UnauthorizedException(ErrorModel errorModel) {
        super(errorModel.getMessage());
        this.errorModel = errorModel;
    }

    public UnauthorizedException(String message) {
        super(message);
        this.errorModel = new ErrorModel(401, message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
        this.errorModel = new ErrorModel(401, message);
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }
}

package ru.pt.api.dto.exception;

import ru.pt.api.dto.errors.ErrorModel;

/**
 * Исключение для ошибок валидации (422 Unprocessable Entity)
 * Используется когда запрос синтаксически корректен, но семантически некорректен
 */
public class UnprocessableEntityException extends RuntimeException {
    private final ErrorModel errorModel;

    public UnprocessableEntityException(ErrorModel errorModel) {
        super(errorModel.getMessage());
        this.errorModel = errorModel;
    }

    public UnprocessableEntityException(String message) {
        super(message);
        this.errorModel = new ErrorModel(422, message);
    }

    public UnprocessableEntityException(String message, Throwable cause) {
        super(message, cause);
        this.errorModel = new ErrorModel(422, message);
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }
}

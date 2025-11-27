package ru.pt.api.dto.exception;


import ru.pt.api.dto.errors.ErrorModel;

public class InternalServerErrorException extends RuntimeException {
    private final ErrorModel errorModel;

    public InternalServerErrorException(ErrorModel errorModel) {
        super(errorModel.getMessage());
        this.errorModel = errorModel;
    }

    public InternalServerErrorException(String message) {
        super(message);
        this.errorModel = new ErrorModel(500, message);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause);
        this.errorModel = new ErrorModel(500, message);
    }

    public ErrorModel getErrorModel() {
        return errorModel;
    }
}

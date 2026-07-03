package ru.pt.api.dto.exception;

/**
 * LLM не настроен или отключён для tenant.
 */
public class LlmUnavailableException extends UnprocessableEntityException {

    public static final String DEFAULT_MESSAGE =
            "LLM недоступен. Настройте интеграцию в параметрах тенанта.";

    public LlmUnavailableException() {
        super(DEFAULT_MESSAGE, "LLM", "NOT_CONFIGURED", null);
    }

    public LlmUnavailableException(String reason) {
        super(DEFAULT_MESSAGE, "LLM", reason, null);
    }
}

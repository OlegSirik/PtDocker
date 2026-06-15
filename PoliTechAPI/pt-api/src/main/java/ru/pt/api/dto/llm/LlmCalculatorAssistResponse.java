package ru.pt.api.dto.llm;

import com.fasterxml.jackson.annotation.JsonInclude;
import ru.pt.api.dto.calculator.CalculatorModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlmCalculatorAssistResponse {

    private boolean success;
    private CalculatorModel calculator;
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public CalculatorModel getCalculator() {
        return calculator;
    }

    public void setCalculator(CalculatorModel calculator) {
        this.calculator = calculator;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

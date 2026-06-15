package ru.pt.api.dto.llm;

import ru.pt.api.dto.calculator.CalculatorModel;

public class LlmCalculatorAssistRequest {

    private String userMessage;
    private CalculatorModel calculator;
    private String providerCode;
    private String model;

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public CalculatorModel getCalculator() {
        return calculator;
    }

    public void setCalculator(CalculatorModel calculator) {
        this.calculator = calculator;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}

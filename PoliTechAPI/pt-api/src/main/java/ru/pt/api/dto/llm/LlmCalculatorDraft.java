package ru.pt.api.dto.llm;

import com.fasterxml.jackson.databind.JsonNode;

public class LlmCalculatorDraft {

    private JsonNode calculator;

    public JsonNode getCalculator() {
        return calculator;
    }

    public void setCalculator(JsonNode calculator) {
        this.calculator = calculator;
    }
}

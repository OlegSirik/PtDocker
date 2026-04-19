package ru.pt.api.dto.calculator;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculatorTemplateLine {
    private Long calculatorId;
    private Long lineNr;
    private String text;

    public Long getCalculatorId() {
        return calculatorId;
    }

    public void setCalculatorId(Long calculatorId) {
        this.calculatorId = calculatorId;
    }

    public Long getLineNr() {
        return lineNr;
    }

    public void setLineNr(Long lineNr) {
        this.lineNr = lineNr;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

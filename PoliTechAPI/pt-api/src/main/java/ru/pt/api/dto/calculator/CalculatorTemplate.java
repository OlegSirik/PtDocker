package ru.pt.api.dto.calculator;

import java.util.List;
import lombok.Data;

@Data
public class CalculatorTemplate {

    private Long calculatorId;
    private String calculatorName;
    private List<CalculatorTemplateLine> lines;

    public CalculatorTemplate(Long calculatorId1, String calculatorName1, List<CalculatorTemplateLine> lines) {
        this.calculatorId = calculatorId1;
        this.calculatorName = calculatorName1;
        this.lines = lines;
    }

}

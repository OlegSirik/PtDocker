package ru.pt.api.dto.calculator;

import java.util.ArrayList;
import java.util.List;

public class CoefficientDataRow {
    private Integer id;
    private List<String> conditionValue = new ArrayList<>();
    private Double resultValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<String> getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(List<String> conditionValue) {
        this.conditionValue = conditionValue;
    }

    public Double getResultValue() {
        return resultValue;
    }

    public void setResultValue(Double resultValue) {
        this.resultValue = resultValue;
    }
}

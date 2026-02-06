package ru.pt.api.dto.calculator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CoefficientDef {
    private String varCode;
    private String varName;
    private String altVarCode;
    private BigDecimal altVarValue;

    private List<CoefficientColumn> columns = new ArrayList<>();

    public String getVarCode() {
        return varCode;
    }

    public void setVarCode(String varCode) {
        this.varCode = varCode;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public List<CoefficientColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<CoefficientColumn> columns) {
        this.columns = columns;
    }

    public void setAltVarCode(String altVarCode) {
        this.altVarCode = altVarCode;
    }

    public String getAltVarCode() {
        return altVarCode;
    }

    public void setAltVarValue(BigDecimal value) {
        this.altVarValue = value;
    }

    public BigDecimal getAltVarValue() {
        return this.altVarValue;
    }
}
package ru.pt.api.dto.llm;

import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.FormulaDef;

import java.util.ArrayList;
import java.util.List;

public class LlmCalculatorDraft {

    private String packageNo;
    private List<FormulaDef> formulas = new ArrayList<>();
    private List<CoefficientDef> coefficients = new ArrayList<>();

    public String getPackageNo() {
        return packageNo;
    }

    public void setPackageNo(String packageNo) {
        this.packageNo = packageNo;
    }

    public List<FormulaDef> getFormulas() {
        return formulas;
    }

    public void setFormulas(List<FormulaDef> formulas) {
        this.formulas = formulas != null ? formulas : new ArrayList<>();
    }

    public List<CoefficientDef> getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(List<CoefficientDef> coefficients) {
        this.coefficients = coefficients != null ? coefficients : new ArrayList<>();
    }
}

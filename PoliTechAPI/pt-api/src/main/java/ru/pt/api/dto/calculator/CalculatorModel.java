package ru.pt.api.dto.calculator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CalculatorModel {
    private Long id;
    private Long productId;
    private String productCode;
    private Long versionNo;
    private String packageNo;
    private String llmText;
    private List<CalculatorVar> vars = new ArrayList<>();
    private List<FormulaDef> formulas = new ArrayList<>();
    private List<CoefficientDef> coefficients = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public Long getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Long versionNo) {
        this.versionNo = versionNo;
    }

    public String getPackageNo() {
        return packageNo;
    }

    public void setPackageNo(String packageNo) {
        this.packageNo = packageNo;
    }

    public String getLlmText() {
        return llmText;
    }

    public void setLlmText(String llmText) {
        this.llmText = llmText;
    }

    public List<CalculatorVar> getVars() {
        return vars;
    }

    public void setVars(List<CalculatorVar> vars) {
        this.vars = vars != null ? vars : new ArrayList<>();
    }

    public List<FormulaDef> getFormulas() {
        return formulas;
    }

    public void setFormulas(List<FormulaDef> formulas) {
        this.formulas = formulas;
    }

    public List<CoefficientDef> getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(List<CoefficientDef> coefficients) {
        this.coefficients = coefficients;
    }
}

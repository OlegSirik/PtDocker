package ru.pt.api.dto.calculator;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.pt.api.dto.product.VarDataType;

/**
 * Узел словаря калькулятора. Не путать с product {@code PvVar}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculatorVar {

    @JsonProperty("varCode")
    private String varCode;

    @JsonProperty("varName")
    private String varName;

    /** Канон: POLICY / CONST / COEFFICIENT / VAR / COVER. */
    @JsonProperty("varKind")
    @JsonAlias("kind")
    private CalcVarKind varKind;

    /**
     * Legacy поле из {@code PvVar}; при чтении заполняет {@link #varKind}.
     */
    @JsonProperty("varType")
    private String varType;

    @JsonProperty("varDataType")
    private VarDataType varDataType;

    @JsonProperty("varValue")
    private String varValue;

    /** COVER / system-generated. */
    @JsonProperty("system")
    private Boolean system;

    /** Legacy {@code varCdm}; только чтение для миграции. */
    @JsonProperty("varCdm")
    private String varCdm;

    public CalculatorVar() {
    }

    public CalculatorVar(String varCode, String varName, CalcVarKind varKind, VarDataType varDataType) {
        this.varCode = varCode;
        this.varName = varName;
        this.varKind = varKind;
        this.varDataType = varDataType;
    }

    public static CalculatorVar coverSumInsured(String coverCode) {
        CalculatorVar v = new CalculatorVar(
                "co_" + coverCode + "_sumInsured",
                coverCode + " страховая сумма",
                CalcVarKind.COVER,
                VarDataType.NUMBER);
        v.setSystem(true);
        return v;
    }

    public static CalculatorVar coverPremium(String coverCode) {
        CalculatorVar v = new CalculatorVar(
                "co_" + coverCode + "_premium",
                coverCode + " премия" ,
                CalcVarKind.COVER,
                VarDataType.NUMBER);
        v.setSystem(true);
        return v;
    }

    public static CalculatorVar coverDeductibleNr(String coverCode) {
        CalculatorVar v = new CalculatorVar(
                "co_" + coverCode + "_deductibleNr",
                coverCode + " id франшизы",
                CalcVarKind.COVER,
                VarDataType.NUMBER);
        v.setSystem(true);
        return v;
    }

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

    public CalcVarKind getVarKind() {
        return varKind;
    }

    public void setVarKind(CalcVarKind varKind) {
        this.varKind = varKind;
    }

    /** Для обратной совместимости ответов UI: дублирует varKind. */
    public String getVarType() {
        if (varKind != null) {
            return varKind.name();
        }
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
        if (this.varKind == null) {
            this.varKind = CalcVarKind.parse(varType);
        }
    }

    public VarDataType getVarDataType() {
        return varDataType;
    }

    public void setVarDataType(VarDataType varDataType) {
        this.varDataType = varDataType;
    }

    public String getVarValue() {
        return varValue == null ? "" : varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    public boolean isSystem() {
        return Boolean.TRUE.equals(system);
    }

    public String getVarCdm() {
        return varCdm;
    }

    public void setVarCdm(String varCdm) {
        this.varCdm = varCdm;
    }
}

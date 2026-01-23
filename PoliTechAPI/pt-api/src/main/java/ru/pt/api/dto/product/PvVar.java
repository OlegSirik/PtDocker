package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PvVar {

    @JsonProperty("varDataType")
    private VarDataType varDataType;

    @JsonProperty("varCode")
    private String varCode;

    @JsonProperty("varName")
    private String varName;

    @JsonProperty("varPath")
    private String varPath;

    @JsonProperty("varType")
    private String varType;

    @JsonProperty("varValue")
    private String varValue = "";

    @JsonProperty("varCdm")
    private String varCdm;

    @JsonProperty("varNr")
    private String varNr;

    // Constructors
    public PvVar() {}

    public PvVar(String varCode, String varName, String varPath, String varType, VarDataType varDataType) {
        this.varCode = varCode;
        this.varName = varName;
        this.varPath = varPath;
        this.varType = varType;
        this.varDataType = varDataType;
    }
    public PvVar(String varCode, String varName, String varPath, String varType, String varValue, VarDataType varDataType) {
        this.varCode = varCode;
        this.varName = varName;
        this.varPath = varPath;
        this.varType = varType;
        this.varValue = varValue;
        this.varDataType = varDataType;
    }

    public PvVar(String varCode, String varName, String varPath, String varType, String varValue, VarDataType varDataType, String varCdm, String varNr) {
        this.varCode = varCode;
        this.varName = varName;
        this.varPath = varPath;
        this.varType = varType;
        this.varValue = varValue;
        this.varDataType = varDataType;
        this.varCdm = varCdm;
        this.varNr = varNr;
    }

    // Getters and Setters
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

    public String getVarPath() {
        return varPath;
    }

    public void setVarPath(String varPath) {
        this.varPath = varPath;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    public String getVarValue() {
        if (varValue == null) varValue = "";
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public VarDataType getVarDataType() {
        return varDataType;
    }

    public void setVarDataType(VarDataType varDataType) {
        this.varDataType = varDataType;
    }

    public String getVarCdm() {
        return varCdm;
    }

    public void setVarCdm(String varCdm) {
        this.varCdm = varCdm;
    }

    public String getVarNr() {
        return varNr;
    }

    public void setVarNr(String varNr) {
        this.varNr = varNr;
    }

    public static PvVar varSumInsured(String coverCode) {
        return new PvVar("co_" + coverCode + "_sumInsured"
        , "Страховая сумма по  " + coverCode
        , "insuredObjects[0].covers[?(@.cover.code == \"" + coverCode + "\")].sumInsured"
        , "VAR"
        , ""
        , VarDataType.NUMBER
        , "coverage.co_" + coverCode + "_sumInsured"
        , "1001");
    }
    public static PvVar varPremium(String coverCode) {
        return new PvVar("co_" + coverCode + "_premium"
        , "Премия по  " + coverCode
        , "insuredObjects[0].covers[?(@.cover.code == \"" + coverCode + "\")].premium"
        , "VAR", "", VarDataType.NUMBER, "coverage.co_" + coverCode + "_premium", "1001");
    }
    public static PvVar varDeductibleNr(String coverCode) {
        return new PvVar("co_" + coverCode + "_deductibleNr"
        , "Id франшизы по  " + coverCode
        , "insuredObjects[0].covers[?(@.cover.code == \"" + coverCode + "\")].deductibleNr"
        , "VAR", "", VarDataType.NUMBER, "coverage.co_" + coverCode + "_deductibleNr", "1001");
    }
    public static PvVar varLimitMin(String coverCode) {
        return new PvVar("co_" + coverCode + "_limitMin"
        , "Лимит ответственности (min) по  " + coverCode
        , "insuredObjects[0].covers[?(@.cover.code == \"" + coverCode + "\")].limitMin"
        , "VAR", "", VarDataType.NUMBER, "coverage.co_" + coverCode + "_limitMin", "1001");
    }
    public static PvVar varLimitMax(String coverCode) {
        return new PvVar("co_" + coverCode + "_limitMax"
        , "Лимит ответственности (max) по  " + coverCode
        , "insuredObjects[0].covers[?(@.cover.code == \"" + coverCode + "\")].limitMax"
        , "VAR", "", VarDataType.NUMBER, "coverage.co_" + coverCode + "_limitMax", "1001");
    }

    // Policy
    public static final String POLICY_NUMBER = "pl_policy_nr";
    public static final String POLICY_STATUS = "pl_status";
    public static final String POLICY_VERSION = "pl_version";

    // Product
    public static final String PRODUCT_CODE = "pl_product_code";
    public static final String PRODUCT_VERSION = "pl_product_version";

}

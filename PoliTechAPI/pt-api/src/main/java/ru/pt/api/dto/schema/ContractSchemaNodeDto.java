package ru.pt.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.pt.api.dto.product.VarDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Узел дерева схемы для экспорта/импорта.
 * Связь parent/child — через вложенный {@code children}, без {@code id}/{@code parent_id}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ContractSchemaNodeDto {

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
    private String varValue;

    @JsonProperty("varNr")
    private String varNr;

    @JsonProperty("varList")
    private String varList;

    @JsonProperty("isSystem")
    private boolean isSystem;

    @JsonProperty("defaultState")
    private String defaultState;

    @JsonProperty("children")
    private List<ContractSchemaNodeDto> children = new ArrayList<>();

    public ContractSchemaNodeDto() {
    }

    public VarDataType getVarDataType() {
        return varDataType;
    }

    public void setVarDataType(VarDataType varDataType) {
        this.varDataType = varDataType;
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
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public String getVarNr() {
        return varNr;
    }

    public void setVarNr(String varNr) {
        this.varNr = varNr;
    }

    public String getVarList() {
        return varList;
    }

    public void setVarList(String varList) {
        this.varList = varList;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public String getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(String defaultState) {
        this.defaultState = defaultState;
    }

    public List<ContractSchemaNodeDto> getChildren() {
        return children;
    }

    public void setChildren(List<ContractSchemaNodeDto> children) {
        this.children = children != null ? children : new ArrayList<>();
    }
}

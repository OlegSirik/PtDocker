package ru.pt.api.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Draft schema attribute for preview; applied via {@code SchemaService.addAttribute}.
 */
public class LlmSchemaAttributeDraft {

    private String varCode;
    private String varName;
    private String varPath;
    private String varType;
    private String varDataType;
    private String varList;
    private String varValue;
    private Boolean isSystem;

    @JsonProperty("parent_id")
    private Long parentId;

    /** Prefer resolving parent by code when parent_id is unknown to the model. */
    private String parentVarCode;

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

    public String getVarDataType() {
        return varDataType;
    }

    public void setVarDataType(String varDataType) {
        this.varDataType = varDataType;
    }

    public String getVarList() {
        return varList;
    }

    public void setVarList(String varList) {
        this.varList = varList;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean system) {
        isSystem = system;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentVarCode() {
        return parentVarCode;
    }

    public void setParentVarCode(String parentVarCode) {
        this.parentVarCode = parentVarCode;
    }
}

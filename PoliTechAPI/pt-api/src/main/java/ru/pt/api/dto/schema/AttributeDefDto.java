package ru.pt.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for attribute definition (legacy/simple). Display name = {@code varCode} context via callers.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeDefDto {
    private String dataType;
    private Long nr;
    private String varCode;
    private String varValue;

    public AttributeDefDto() {
    }

    public AttributeDefDto(String dataType, Long nr, String varCode, String varValue) {
        this.dataType = dataType;
        this.nr = nr;
        this.varCode = varCode;
        this.varValue = varValue;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Long getNr() {
        return nr;
    }

    public void setNr(Long nr) {
        this.nr = nr;
    }

    public String getVarCode() {
        return varCode;
    }

    public void setVarCode(String varCode) {
        this.varCode = varCode;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }
}

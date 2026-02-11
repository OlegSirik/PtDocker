package ru.pt.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for attribute definition
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttributeDefDto {
    private String code;
    private String name;
    private String dataType;
    private Long nr;
    private String varCode;
    private String varValue;

    public AttributeDefDto() {
    }

    public AttributeDefDto(String code, String name, String dataType, Long nr, String varCode, String varValue) {
        this.code = code;
        this.name = name;
        this.dataType = dataType;
        this.nr = nr;
        this.varCode = varCode;
        this.varValue = varValue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

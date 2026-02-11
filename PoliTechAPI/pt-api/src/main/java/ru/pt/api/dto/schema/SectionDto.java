package ru.pt.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for contract section
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectionDto {
    private String code;
    private String name;

    public SectionDto() {
    }

    public SectionDto(String code, String name) {
        this.code = code;
        this.name = name;
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
}

package ru.pt.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO for entity definition
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EntityDefDto {
    private String code;
    private String name;

    public EntityDefDto() {
    }

    public EntityDefDto(String code, String name) {
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

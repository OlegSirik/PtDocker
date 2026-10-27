package ru.pt.api.dto.schema;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Экспорт/импорт модели договора: {@code mt_contract_model} + дерево атрибутов.
 * {@code code} = {@code document_id}. Дерево — вложенные {@code children}, без переносимых id.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractSchemaExportDto {

    private String code;
    private String name;
    /** Корни дерева атрибутов (каждый узел может иметь {@code children}). */
    private List<ContractSchemaNodeDto> attributes = new ArrayList<>();

    public ContractSchemaExportDto() {
    }

    public ContractSchemaExportDto(String code, String name, List<ContractSchemaNodeDto> attributes) {
        this.code = code;
        this.name = name;
        this.attributes = attributes != null ? attributes : new ArrayList<>();
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

    public List<ContractSchemaNodeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ContractSchemaNodeDto> attributes) {
        this.attributes = attributes != null ? attributes : new ArrayList<>();
    }
}

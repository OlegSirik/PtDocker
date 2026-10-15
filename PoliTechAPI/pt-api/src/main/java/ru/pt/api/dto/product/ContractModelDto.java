package ru.pt.api.dto.product;

/**
 * Модель договора ({@code mt_contract_model}): {@code code} = {@code document_id} в {@code mt_attribute_def}.
 */
public class ContractModelDto {

    private Long id;
    private String code;
    private String name;

    public ContractModelDto() {
    }

    public ContractModelDto(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

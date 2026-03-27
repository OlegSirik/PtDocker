package ru.pt.product.entity;

import jakarta.persistence.*;

/**
 * JPA-сущность для {@code mt_attribute_def}: дерево атрибутов схемы договора.
 * API-обмен через DTO {@link ru.pt.api.dto.product.LobVar}; отдельный сервис не используется — операции в {@link ru.pt.api.service.schema.SchemaService}.
 */
@Entity
@Table(
        name = "mt_attribute_def",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attribute_def",
                columnNames = {"tenant_id", "document_id", "var_code"}
        )
)
public class AttributeDefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mt_attribute_def_gen")
    @SequenceGenerator(name = "mt_attribute_def_gen", sequenceName = "mt_attribute_def_seq", allocationSize = 1)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "document_id", nullable = false, length = 30)
    private String documentId;

    @Column(name = "var_code", nullable = false, length = 50)
    private String varCode;

    @Column(name = "var_name", nullable = false, length = 300)
    private String varName;

    @Column(name = "var_path", nullable = false, length = 500)
    private String varPath;

    @Column(name = "var_ord", nullable = false)
    private Long varOrd = 0L;

    @Column(name = "var_type", nullable = false, length = 30)
    private String varType;

    @Column(name = "var_cardinality", nullable = false, length = 10)
    private String varCardinality = "SINGLE";

    @Column(name = "var_data_type", nullable = false, length = 20)
    private String varDataType;

    @Column(name = "var_value", length = 500)
    private String varValue;

    @Column(name = "var_cdm", length = 500)
    private String varCdm;

    @Column(name = "var_list", length = 100)
    private String varList;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    public AttributeDefEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
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

    public Long getVarOrd() {
        return varOrd;
    }

    public void setVarOrd(Long varOrd) {
        this.varOrd = varOrd;
    }

    public String getVarType() {
        return varType;
    }

    public void setVarType(String varType) {
        this.varType = varType;
    }

    public String getVarCardinality() {
        return varCardinality;
    }

    public void setVarCardinality(String varCardinality) {
        this.varCardinality = varCardinality;
    }

    public String getVarDataType() {
        return varDataType;
    }

    public void setVarDataType(String varDataType) {
        this.varDataType = varDataType;
    }

    public String getVarValue() {
        return varValue;
    }

    public void setVarValue(String varValue) {
        this.varValue = varValue;
    }

    public String getVarCdm() {
        return varCdm;
    }

    public void setVarCdm(String varCdm) {
        this.varCdm = varCdm;
    }

    public String getVarList() {
        return varList;
    }

    public void setVarList(String varList) {
        this.varList = varList;
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

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }
}

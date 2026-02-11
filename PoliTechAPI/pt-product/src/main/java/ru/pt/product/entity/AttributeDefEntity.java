package ru.pt.product.entity;

import jakarta.persistence.*;

/**
 * Entity for attribute definition (mt_attribute_def)
 */
@Entity
@Table(name = "mt_attribute_def", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"entity_id", "code"}),
    @UniqueConstraint(columnNames = {"entity_id", "name"})
})
public class AttributeDefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mt_seq")
    @SequenceGenerator(name = "mt_seq", sequenceName = "mt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entity_id", insertable = false, updatable = false)
    private EntityDefEntity entity;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "path", nullable = false, length = 100)
    private String path;

    @Column(name = "nr", nullable = false)
    private Long nr;

    @Column(name = "var_code", nullable = false, length = 50)
    private String varCode;

    @Column(name = "var_name", nullable = false, length = 300)
    private String varName;

    @Column(name = "var_path", nullable = false, length = 100)
    private String varPath;

    @Column(name = "var_type", nullable = false, length = 20)
    private String varType;

    @Column(name = "var_value", length = 500)
    private String varValue;

    @Column(name = "var_cdm", nullable = false, length = 100)
    private String varCdm;

    @Column(name = "var_data_type", nullable = false, length = 10)
    private String varDataType; // STRING or NUMBER

    public AttributeDefEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public EntityDefEntity getEntity() {
        return entity;
    }

    public void setEntity(EntityDefEntity entity) {
        this.entity = entity;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public String getVarCdm() {
        return varCdm;
    }

    public void setVarCdm(String varCdm) {
        this.varCdm = varCdm;
    }

    public String getVarDataType() {
        return varDataType;
    }

    public void setVarDataType(String varDataType) {
        this.varDataType = varDataType;
    }
}

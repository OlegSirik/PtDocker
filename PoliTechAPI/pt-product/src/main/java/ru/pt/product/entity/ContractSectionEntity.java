package ru.pt.product.entity;

import jakarta.persistence.*;

/**
 * Entity for contract section (mt_contract_section)
 */
@Entity
@Table(name = "mt_contract_section", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"model_id", "code"}),
    @UniqueConstraint(columnNames = {"model_id", "name"})
})
public class ContractSectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mt_seq")
    @SequenceGenerator(name = "mt_seq", sequenceName = "mt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid", nullable = false)
    private Long tid;

    @Column(name = "model_id", nullable = false)
    private Long modelId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", insertable = false, updatable = false)
    private ContractModelEntity model;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 300)
    private String name;

    @Column(name = "path", length = 100)
    private String path;

    public ContractSectionEntity() {
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

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }

    public ContractModelEntity getModel() {
        return model;
    }

    public void setModel(ContractModelEntity model) {
        this.model = model;
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
}

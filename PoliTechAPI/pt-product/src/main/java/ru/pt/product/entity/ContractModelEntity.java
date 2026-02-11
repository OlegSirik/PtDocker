package ru.pt.product.entity;

import jakarta.persistence.*;

/**
 * Entity for contract model (mt_contract_model)
 */
@Entity
@Table(name = "mt_contract_model", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"tid", "code"}),
    @UniqueConstraint(columnNames = {"tid", "name"})
})
public class ContractModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mt_seq")
    @SequenceGenerator(name = "mt_seq", sequenceName = "mt_seq", allocationSize = 1)
    private Long id;

    @Column(name = "tid")
    private Long tid;

    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "name", length = 300)
    private String name;

    public ContractModelEntity() {
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

package ru.pt.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

/**
 * Entity for storing complete policy data as JSON
 */
@Entity
@Table(name = "policy_data")
public class PolicyEntity {

    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_seq")
    //@SequenceGenerator(name = "policy_seq", sequenceName = "policy_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "tid")
    private Long tid;

    @Column(name = "cid")
    private Long cid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy", columnDefinition = "jsonb", nullable = false)
    private String policy;

    public PolicyEntity() {
    }

    public PolicyEntity(Long id, Long tid, String policy) {
        this.id = id;
        this.policy = policy;
        this.tid = tid;
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

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }
}

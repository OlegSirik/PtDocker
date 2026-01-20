package ru.pt.db.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entity for storing complete policy data as JSON
 */
@Entity
@Table(name = "policy_data")
public class PolicyEntity {

    @Id
    private UUID id;

    @Column(name = "tid")
    private Long tid;

    @Column(name = "cid")
    private Long cid;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "policy", columnDefinition = "jsonb", nullable = false)
    private String policy;

    public PolicyEntity() {
    }

    public PolicyEntity(UUID id, Long tid, String policy) {
        this.id = id;
        this.policy = policy;
        this.tid = tid;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

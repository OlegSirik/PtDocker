package ru.pt.product.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "pt_lobs")
public class LobEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 128)
    private String code;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "lob", columnDefinition = "jsonb", nullable = false)
    private String lob;

    @Column(name = "record_status", nullable = false)
    private String recordStatus = "ACTIVE";

    @Column(name = "tid", nullable = false)
    private Long tId;

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

    public String getLob() {
        return lob;
    }

    public void setLob(String lob) {
        this.lob = lob;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }

    public Long getTid() {
        return tId;
    }

    public void setTid(Long tid) {
        this.tId = tid;
    }
}
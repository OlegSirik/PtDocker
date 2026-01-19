package ru.pt.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "acc_tenants")
public class TenantEntity {
    public static final String SYS_TENANT_CODE = "sys";

    @Id
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    //@SequenceGenerator(name = "account_seq", sequenceName = "account_seq", allocationSize = 1)
    private Long id;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "code", length = 100, unique = true, nullable = false)
    private String code;

    @Column(name = "auth_type", length = 20)
    private String authType;
    // constructors, getters, setters
    public TenantEntity() {}

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

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAuthType() {return authType;}

    public void setAuthType(String authType) {this.authType = authType;}

    public boolean isSystem() {return SYS_TENANT_CODE.equals(code);}

    private TenantEntity(String code, String name, String authType) {
        this.code = code;
        this.name = name;
        this.authType = authType;
        this.isDeleted = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static TenantEntity create(String code, String name, String authType) {
        return new TenantEntity(code, name, authType);
    }
}
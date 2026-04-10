package ru.pt.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "acc_tenants")
public class TenantEntity {
    public static final String SYS_TENANT_CODE = "sys";

    @Id
    private Long id;

    @Column(name = "name", length = 250)
    private String name;

    @Column(name = "record_status")
    private String recordStatus = "ACTIVE";

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

    @Column(name = "storage_type", length = 20)
    private String storageType;

    @Column(name = "storage_config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> storageConfig = new HashMap<>();

    @Column(name = "auth_config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> authConfig = new HashMap<>();

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

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
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

    public String getStorageType() {return storageType;}

    public void setStorageType(String storageType) {this.storageType = storageType;}

    public Map<String, String> getStorageConfig() {
        return storageConfig;
    }

    public void setStorageConfig(Map<String, String> storageConfig) {
        this.storageConfig = storageConfig != null ? storageConfig : new HashMap<>();
    }

    public Map<String, String> getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(Map<String, String> authConfig) {
        this.authConfig = authConfig != null ? authConfig : new HashMap<>();
    }

    public boolean isSystem() {return SYS_TENANT_CODE.equals(code);}

    private TenantEntity(String code, String name, String authType) {
        this.code = code;
        this.name = name;
        this.authType = authType;
        this.recordStatus = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static TenantEntity create(String code, String name, String authType) {
        return new TenantEntity(code, name, authType);
    }
}
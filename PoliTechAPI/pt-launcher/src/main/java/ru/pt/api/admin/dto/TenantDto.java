package ru.pt.api.admin.dto;

import java.time.LocalDateTime;

public class TenantDto {
    private Long id;
    private String name;
    private String code;
    private String recordStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // constructors
    public TenantDto() {
    }

    // getters and setters
    public Long getId() {
        return id;
    }   

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
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
}

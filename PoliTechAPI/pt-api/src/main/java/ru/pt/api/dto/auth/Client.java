package ru.pt.api.dto.auth;

import java.time.LocalDateTime;

public class Client {
    private Long id;
    private Long tid;
    private String clientId;
    private Long defaultAccountId;
    private Long clientAccountId;
    private String name;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ClientConfiguration clientConfiguration;
    private String authType;

    // constructors
    public Client() {
    }

    // getters and setters
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Long getDefaultAccountId() {
        return defaultAccountId;
    }

    public void setDefaultAccountId(Long defaultAccountId) {
        this.defaultAccountId = defaultAccountId;
    }

    public Long getClientAccountId() {
        return clientAccountId;
    }

    public void setClientAccountId(Long clientAccountId) {
        this.clientAccountId = clientAccountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
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

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

}
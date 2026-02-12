package ru.pt.api.dto.auth;

import ru.pt.api.dto.product.Product;

public class Client {
    private Long id;
    private Long tid;
    private String clientId;
    private Long defaultAccountId;
    private Long clientAccountId;
    private String name;
    private Boolean isDeleted;
    private ClientConfiguration clientConfiguration;
    private String authType;
    private String authLevel;
    private Product[] products;

    public Client() {
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

    public Product[] getProducts() {
        return products;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }

    public void setAuthLevel(String authLevel) {
        this.authLevel = authLevel;
    }

    public String getAuthLevel() {
        return this.authLevel;
    }
}
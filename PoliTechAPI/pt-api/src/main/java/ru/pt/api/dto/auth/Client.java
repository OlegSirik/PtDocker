package ru.pt.api.dto.auth;

import ru.pt.api.dto.product.Product;
import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.api.dto.auth.ClientAuthType;
import ru.pt.api.dto.auth.ClientAuthLevel;

public class Client {
    private Long id;
    private Long tid;
    private String authClientId;
    private Long defaultAccountId;
    private Long clientAccountId;
    private String name;
    private RecordStatus recordStatus;
    private ClientConfiguration clientConfiguration;
    private ClientAuthType authType;
    private ClientAuthLevel authLevel;
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

    public String getAuthClientId() {
        return authClientId;
    }

    public void setAuthClientId(String authClientId) {
        this.authClientId = authClientId;
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

    public RecordStatus getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(RecordStatus recordStatus) {
        this.recordStatus = recordStatus;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public void setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
    }

    public ClientAuthType getAuthType() {
        return authType;
    }

    public void setAuthType(ClientAuthType authType) {
        this.authType = authType;
    }

    public Product[] getProducts() {
        return products;
    }

    public void setProducts(Product[] products) {
        this.products = products;
    }

    public void setAuthLevel(ClientAuthLevel authLevel) {
        this.authLevel = authLevel;
    }

    public ClientAuthLevel getAuthLevel() {
        return authLevel;
    }
}
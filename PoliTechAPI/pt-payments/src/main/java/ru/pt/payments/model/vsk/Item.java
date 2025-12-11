package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    @JsonProperty("typeId")
    private String typeId;
    @JsonProperty("documentTypeId")
    private String documentTypeId;
    @JsonProperty("documentId")
    private String documentId;
    @JsonProperty("cost")
    private String cost;
    @JsonProperty("attributes")
    private ItemAttributes attributes;
    @JsonProperty("accountId")
    private String accountId;

    public Item() {
    }

    public Item(String typeId, String documentTypeId, String documentId, String cost, ItemAttributes attributes, String accountId) {
        this.typeId = typeId;
        this.documentTypeId = documentTypeId;
        this.documentId = documentId;
        this.cost = cost;
        this.attributes = attributes;
        this.accountId = accountId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(String documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public ItemAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(ItemAttributes attributes) {
        this.attributes = attributes;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}

package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class Lot {

    @JsonProperty("typeId")
    private String typeId;
    @JsonProperty("cost")
    private BigDecimal cost;
    @JsonProperty("attributes")
    private LotAttributes attributes;
    @JsonProperty("accountId")
    private String accountId;

    public Lot() {
    }

    public Lot(String typeId, BigDecimal cost, LotAttributes attributes, String accountId) {
        this.typeId = typeId;
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

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LotAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(LotAttributes attributes) {
        this.attributes = attributes;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

}

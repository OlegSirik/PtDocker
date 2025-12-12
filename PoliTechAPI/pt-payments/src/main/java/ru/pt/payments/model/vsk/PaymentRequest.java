package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentRequest {

    @JsonProperty("typeId")
    private String typeId;
    @JsonProperty("currencyID")
    private String currencyId;
    @JsonProperty("attributes")
    private PaymentRequestAttributes attributes;

    @JsonProperty("items")
    private List<Item> items;
    @JsonProperty("lots")
    private List<Lot> lots;

    public PaymentRequest() {
    }

    public PaymentRequest(String typeId, String currencyId, PaymentRequestAttributes attributes, List<Item> items, List<Lot> lots) {
        this.typeId = typeId;
        this.currencyId = currencyId;
        this.attributes = attributes;
        this.items = items;
        this.lots = lots;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public PaymentRequestAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(PaymentRequestAttributes attributes) {
        this.attributes = attributes;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Lot> getLots() {
        return lots;
    }

    public void setLots(List<Lot> lots) {
        this.lots = lots;
    }

}

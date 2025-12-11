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
}

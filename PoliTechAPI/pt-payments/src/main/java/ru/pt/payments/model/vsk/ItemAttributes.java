package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ItemAttributes {

    @JsonProperty("documentNumber")
    private String documentNumber;

    public ItemAttributes() {
    }

    public ItemAttributes(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

}

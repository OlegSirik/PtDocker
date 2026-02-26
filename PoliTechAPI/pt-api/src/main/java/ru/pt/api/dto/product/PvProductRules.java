package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PvProductRules {
   
    @JsonProperty("insuredEqualsPolicyHolder")
    private boolean insuredEqualsPolicyHolder;

    public boolean isInsuredEqualsPolicyHolder() {
        return insuredEqualsPolicyHolder;
    }

    public void setInsuredEqualsPolicyHolder(boolean insuredEqualsPolicyHolder) {
        this.insuredEqualsPolicyHolder = insuredEqualsPolicyHolder;
    }
}


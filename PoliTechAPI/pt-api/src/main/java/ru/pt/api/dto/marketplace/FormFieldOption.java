package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormFieldOption {

    @JsonProperty("value")
    private String value;

    @JsonProperty("label")
    private String label;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

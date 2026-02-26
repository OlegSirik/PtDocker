package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Section for FormMetadata.
 */
public class FormSection {

    @JsonProperty("code")
    private String code;

    @JsonProperty("label")
    private String label;

    public FormSection() {
    }

    public FormSection(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

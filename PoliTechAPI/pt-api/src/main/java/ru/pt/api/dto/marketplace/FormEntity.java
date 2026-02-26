package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Entity for FormMetadata.
 */
public class FormEntity {

    @JsonProperty("code")
    private String code;

    @JsonProperty("section")
    private String section;

    @JsonProperty("label")
    private String label;

    public FormEntity() {
    }

    public FormEntity(String code, String section, String label) {
        this.code = code;
        this.section = section;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}

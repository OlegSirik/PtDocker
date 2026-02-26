package ru.pt.api.dto.marketplace;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Field for FormMetadata.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormField {

    @JsonProperty("code")
    private String code;

    @JsonProperty("key")
    private String key;

    @JsonProperty("type")
    private String type;

    @JsonProperty("label")
    private String label;

    @JsonProperty("placeholder")
    private String placeholder;

    @JsonProperty("options")
    private List<FormFieldOption> options;

    public FormField() {
    }

    public FormField(String code, String key, String type, String label, String placeholder) {
        this.code = code;
        this.key = key;
        this.type = type;
        this.label = label;
        this.placeholder = placeholder;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public List<FormFieldOption> getOptions() {
        return options;
    }

    public void setOptions(List<FormFieldOption> options) {
        this.options = options;
    }
}

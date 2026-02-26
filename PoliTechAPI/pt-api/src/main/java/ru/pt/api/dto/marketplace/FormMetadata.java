package ru.pt.api.dto.marketplace;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Form metadata for marketplace product detail.
 */
public class FormMetadata {

    @JsonProperty("title")
    private String title;

    @JsonProperty("sections")
    private List<FormSection> sections;

    @JsonProperty("entities")
    private List<FormEntity> entities;

    @JsonProperty("fields")
    private List<FormField> fields;

    public FormMetadata() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<FormSection> getSections() {
        return sections;
    }

    public void setSections(List<FormSection> sections) {
        this.sections = sections;
    }

    public List<FormEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<FormEntity> entities) {
        this.entities = entities;
    }

    public List<FormField> getFields() {
        return fields;
    }

    public void setFields(List<FormField> fields) {
        this.fields = fields;
    }
}

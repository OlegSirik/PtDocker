package ru.pt.api.dto.llm;

import java.util.ArrayList;
import java.util.List;

public class LlmSchemaDraft {

    private List<LlmSchemaAttributeDraft> attributes = new ArrayList<>();

    public List<LlmSchemaAttributeDraft> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LlmSchemaAttributeDraft> attributes) {
        this.attributes = attributes != null ? attributes : new ArrayList<>();
    }
}

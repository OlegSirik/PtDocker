package ru.pt.api.dto.llm;

import java.util.ArrayList;
import java.util.List;

public class LlmSchemaDraft {

    private List<LlmSchemaAttributeDraft> attributes = new ArrayList<>();

    /** New / extended refdicts to create before attributes that reference them via varList. */
    private List<LlmSchemaRefDictDraft> refdicts = new ArrayList<>();

    public List<LlmSchemaAttributeDraft> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<LlmSchemaAttributeDraft> attributes) {
        this.attributes = attributes != null ? attributes : new ArrayList<>();
    }

    public List<LlmSchemaRefDictDraft> getRefdicts() {
        return refdicts;
    }

    public void setRefdicts(List<LlmSchemaRefDictDraft> refdicts) {
        this.refdicts = refdicts != null ? refdicts : new ArrayList<>();
    }
}

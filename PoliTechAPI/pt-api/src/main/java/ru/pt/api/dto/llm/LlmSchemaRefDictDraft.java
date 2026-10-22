package ru.pt.api.dto.llm;

import java.util.ArrayList;
import java.util.List;

/**
 * Draft reference dictionary for schema assist:
 * create {@code pt_refdicts} + {@code pt_refdata}, then assign code to attribute {@code varList}.
 */
public class LlmSchemaRefDictDraft {

    private String code;
    private String name;
    private List<LlmSchemaRefDictItemDraft> items = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LlmSchemaRefDictItemDraft> getItems() {
        return items;
    }

    public void setItems(List<LlmSchemaRefDictItemDraft> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}

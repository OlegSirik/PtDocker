package ru.pt.api.dto.llm;

import java.util.ArrayList;
import java.util.List;

public class LlmPackageDraft {

    private String code;
    private String name;
    private List<LlmPackageCoverDraft> covers = new ArrayList<>();

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

    public List<LlmPackageCoverDraft> getCovers() {
        return covers;
    }

    public void setCovers(List<LlmPackageCoverDraft> covers) {
        this.covers = covers != null ? covers : new ArrayList<>();
    }
}

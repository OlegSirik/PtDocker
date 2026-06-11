package ru.pt.api.dto.llm;

import ru.pt.api.dto.product.PvVar;

import java.util.ArrayList;
import java.util.List;

public class LlmCoverDraft {

    private String code;
    private String name;
    private Boolean isMandatory;
    private List<PvVar> vars = new ArrayList<>();

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

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean mandatory) {
        isMandatory = mandatory;
    }

    public List<PvVar> getVars() {
        return vars;
    }

    public void setVars(List<PvVar> vars) {
        this.vars = vars != null ? vars : new ArrayList<>();
    }
}

package ru.pt.api.dto.policy;

public class CoverInfo {

    private String code;
    private String option;
    private String description;

    public CoverInfo() {
    }

    public CoverInfo(String code, String option, String description) {
        this.code = code;
        this.option = option;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

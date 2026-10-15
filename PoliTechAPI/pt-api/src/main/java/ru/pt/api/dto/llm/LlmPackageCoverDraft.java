package ru.pt.api.dto.llm;

import java.util.ArrayList;
import java.util.List;

public class LlmPackageCoverDraft {

    private String code;
    private Boolean isMandatory;
    private String waitingPeriod;
    private String coverageTerm;
    private Boolean isDeductibleMandatory;
    private List<LlmPackageDeductibleDraft> deductibles = new ArrayList<>();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(Boolean mandatory) {
        isMandatory = mandatory;
    }

    public String getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(String waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    public String getCoverageTerm() {
        return coverageTerm;
    }

    public void setCoverageTerm(String coverageTerm) {
        this.coverageTerm = coverageTerm;
    }

    public Boolean getIsDeductibleMandatory() {
        return isDeductibleMandatory;
    }

    public void setIsDeductibleMandatory(Boolean deductibleMandatory) {
        isDeductibleMandatory = deductibleMandatory;
    }

    public List<LlmPackageDeductibleDraft> getDeductibles() {
        return deductibles;
    }

    public void setDeductibles(List<LlmPackageDeductibleDraft> deductibles) {
        this.deductibles = deductibles != null ? deductibles : new ArrayList<>();
    }
}

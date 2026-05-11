package ru.pt.db.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

public class InstallmentTemplateLine implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("installment_nr")
    private Integer installmentNr;
    private BigDecimal percent;
    @JsonProperty("period_months")
    private Integer periodMonths;

    public Integer getInstallmentNr() {
        return installmentNr;
    }

    public void setInstallmentNr(Integer installmentNr) {
        this.installmentNr = installmentNr;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }

    public Integer getPeriodMonths() {
        return periodMonths;
    }

    public void setPeriodMonths(Integer periodMonths) {
        this.periodMonths = periodMonths;
    }
}

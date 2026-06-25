package ru.pt.api.dto.policy;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class Cover {

    private CoverInfo cover;
    private List<String> risk;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private BigDecimal sumInsured;
    private BigDecimal premium;
    private Deductible deductible;
    private BigDecimal limitMin;
    private BigDecimal limitMax;

    public Cover() {
    }

    public Cover(CoverInfo cover, List<String> risk, ZonedDateTime startDate, ZonedDateTime endDate,
                 BigDecimal sumInsured, BigDecimal premium, Deductible deductible) {
        this.cover = cover;
        this.risk = risk;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sumInsured = sumInsured;
        this.premium = premium;
        this.deductible = deductible;
    }

    public CoverInfo getCover() {
        return cover;
    }

    public void setCover(CoverInfo cover) {
        this.cover = cover;
    }

    public List<String> getRisk() {
        return risk;
    }

    public void setRisk(List<String> risk) {
        this.risk = risk;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(BigDecimal sumInsured) {
        this.sumInsured = sumInsured;
    }

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public Deductible getDeductible() {
        return deductible;
    }

    public void setDeductible(Deductible deductible) {
        this.deductible = deductible;
    }

    public BigDecimal getLimitMin() {
        return limitMin;
    }

    public void setLimitMin(BigDecimal limitMin) {
        this.limitMin = limitMin;
    }

    public BigDecimal getLimitMax() {
        return limitMax;
    }

    public void setLimitMax(BigDecimal limitMax) {
        this.limitMax = limitMax;
    }
}

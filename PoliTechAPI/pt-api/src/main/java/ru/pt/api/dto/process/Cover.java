package ru.pt.api.dto.process;

import java.time.ZonedDateTime;
import java.util.List;
import ru.pt.api.dto.process.Deductible;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cover {

    @JsonProperty("cover")
    private CoverInfo cover;

    @JsonProperty("risk")
    private List<String> risk;

    @JsonProperty("startDate")
    private ZonedDateTime startDate;

    @JsonProperty("endDate")
    private ZonedDateTime endDate;

    @JsonProperty("sumInsured")
    private BigDecimal sumInsured;

    @JsonProperty("premium")
    private BigDecimal premium;

    private Deductible deductible;

    private BigDecimal LimitMin;
    private BigDecimal LimitMax;

    // Constructors
    public Cover() {}

    public Cover(CoverInfo cover, List<String> risk, ZonedDateTime startDate, ZonedDateTime endDate,
                 BigDecimal sumInsured, BigDecimal premium, Deductible deductible) {
        this.cover = cover;
        this.risk = risk;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sumInsured = sumInsured;
        this.premium = premium;
//        this.deductibleType = deductibleType;
        this.deductible = deductible;
    }

    // Getters and Setters
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

    public void setDeductibleId(Integer deductibleId) {
        if (this.deductible == null) {
            this.deductible = new Deductible();
        }
        this.deductible.setId(deductibleId);
    }

    public void setDeductibleText(String deductibleText) {
        if (this.deductible == null) {
            this.deductible = new Deductible();
        }
        this.deductible.setText(deductibleText);
    }   

    public void setDeductible(Integer deductibleId, String deductibleText) {
        if (this.deductible == null) {
            this.deductible = new Deductible();
        }
        this.deductible.setId(deductibleId);
        this.deductible.setText(deductibleText);
    }

    public BigDecimal getLimitMin() {
        return LimitMin;
    }

    public void setLimitMin(BigDecimal limitMin) {
        LimitMin = limitMin;
    }

    public BigDecimal getLimitMax() {
        return LimitMax;
    }

    public void setLimitMax(BigDecimal limitMax) {
        LimitMax = limitMax;
    }

    
}

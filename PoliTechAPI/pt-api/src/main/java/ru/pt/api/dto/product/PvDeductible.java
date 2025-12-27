package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Франшиза
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PvDeductible {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("text")
    private String text;

    @JsonProperty("deductible")
    private Double deductible;

    @JsonProperty("deductibleType")
    private String deductibleType;

    @JsonProperty("deductibleUnit")
    private String deductibleUnit;

    @JsonProperty("deductibleSpecific")
    private String deductibleSpecific;

 

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getDeductible() {
        return deductible;
    }

    public void setDeductible(Double deductible) {
        this.deductible = deductible;
    }

    public String getDeductibleType() {
        return deductibleType;
    }

    public void setDeductibleType(String deductibleType) {
        this.deductibleType = deductibleType;
    }

    public String getDeductibleUnit() {
        return deductibleUnit;
    }

    public void setDeductibleUnit(String deductibleUnit) {
        this.deductibleUnit = deductibleUnit;
    }

    public String getDeductibleSpecific() {
        return deductibleSpecific;
    }

    public void setDeductibleSpecific(String deductibleSpecific) {
        this.deductibleSpecific = deductibleSpecific;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
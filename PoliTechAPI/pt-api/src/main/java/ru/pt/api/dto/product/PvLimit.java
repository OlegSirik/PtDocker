package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class PvLimit {

    @JsonProperty("premium")
    private BigDecimal premium;

    @JsonProperty("sumInsured")
    private BigDecimal sumInsured;

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public BigDecimal getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(BigDecimal sumInsured) {
        this.sumInsured = sumInsured;
    }
}

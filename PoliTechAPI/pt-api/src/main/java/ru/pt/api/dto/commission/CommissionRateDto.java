package ru.pt.api.dto.commission;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO for commission rate configuration.
 * Action values: sale, prolongation, all, etc.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommissionRateDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("accountId")
    private Long accountId;

    @JsonProperty("productId")
    private Integer productId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("rateValue")
    private BigDecimal rateValue;

    @JsonProperty("fixedAmount")
    private BigDecimal fixedAmount;

    @JsonProperty("minAmount")
    private BigDecimal minAmount;

    @JsonProperty("maxAmount")
    private BigDecimal maxAmount;

    /** Minimum commission rate agent can decrease to. */
    @JsonProperty("commissionMinRate")
    private BigDecimal commissionMinRate;

    /** Maximum commission rate agent can decrease from (base rate). */
    @JsonProperty("commissionMaxRate")
    private BigDecimal commissionMaxRate;

    @JsonProperty("agdNumber")
    private String agdNumber;
}

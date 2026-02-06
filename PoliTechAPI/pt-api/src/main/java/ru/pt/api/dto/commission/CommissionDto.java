package ru.pt.api.dto.commission;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO as part of Policy
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CommissionDto {

    /* % кВ переданный агентом в рамках снижения премии */
    @JsonProperty("requestedCommissionRate")
    private BigDecimal requestedCommissionRate;

    /* Примененный % кВ */
    @JsonProperty("appliedCommissionRate")
    private BigDecimal appliedCommissionRate;

    /* сумма кВ */
    @JsonProperty("commissionAmount")
    private BigDecimal commissionAmount;

    @JsonProperty("agdNumber")
    private String agdNumber;
}

package ru.pt.api.dto.policy;

import java.math.BigDecimal;

/**
 * Комиссия в составе договора ({@link StdPolicy}).
 */
public class Commission {

    /** % кВ, переданный агентом в рамках снижения премии */
    private BigDecimal requestedCommissionDiscount;
    /** % кВ, запрошенный агентом */
    private BigDecimal requestedCommissionRate;
    /** применённый % кВ */
    private BigDecimal appliedCommissionRate;
    /** сумма кВ */
    private BigDecimal commissionAmount;
    private String agdNumber;

    public Commission() {
    }

    public Commission(
            BigDecimal requestedCommissionDiscount,
            BigDecimal requestedCommissionRate,
            BigDecimal appliedCommissionRate,
            BigDecimal commissionAmount,
            String agdNumber) {
        this.requestedCommissionDiscount = requestedCommissionDiscount;
        this.requestedCommissionRate = requestedCommissionRate;
        this.appliedCommissionRate = appliedCommissionRate;
        this.commissionAmount = commissionAmount;
        this.agdNumber = agdNumber;
    }

    public BigDecimal getRequestedCommissionDiscount() {
        return requestedCommissionDiscount;
    }

    public void setRequestedCommissionDiscount(BigDecimal requestedCommissionDiscount) {
        this.requestedCommissionDiscount = requestedCommissionDiscount;
    }

    public BigDecimal getRequestedCommissionRate() {
        return requestedCommissionRate;
    }

    public void setRequestedCommissionRate(BigDecimal requestedCommissionRate) {
        this.requestedCommissionRate = requestedCommissionRate;
    }

    public BigDecimal getAppliedCommissionRate() {
        return appliedCommissionRate;
    }

    public void setAppliedCommissionRate(BigDecimal appliedCommissionRate) {
        this.appliedCommissionRate = appliedCommissionRate;
    }

    public BigDecimal getCommissionAmount() {
        return commissionAmount;
    }

    public void setCommissionAmount(BigDecimal commissionAmount) {
        this.commissionAmount = commissionAmount;
    }

    public String getAgdNumber() {
        return agdNumber;
    }

    public void setAgdNumber(String agdNumber) {
        this.agdNumber = agdNumber;
    }
}

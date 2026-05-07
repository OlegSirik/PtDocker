package ru.pt.api.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateInstallmentsRequest {
    private Long policyId;
    private LocalDate startDate;
    private BigDecimal amount;
    private String currency;
    private String installmentType;

    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getInstallmentType() { return installmentType; }
    public void setInstallmentType(String installmentType) { this.installmentType = installmentType; }
}


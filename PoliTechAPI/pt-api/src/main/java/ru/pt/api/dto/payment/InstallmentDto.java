package ru.pt.api.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InstallmentDto {
    private Long id;
    private Long policyId;
    private Long installmentNr;
    private LocalDate dueDate;
    private BigDecimal amount;
    private String currency;
    private InstallmentStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public Long getInstallmentNr() { return installmentNr; }
    public void setInstallmentNr(Long installmentNr) { this.installmentNr = installmentNr; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public InstallmentStatus getStatus() { return status; }
    public void setStatus(InstallmentStatus status) { this.status = status; }
}


package ru.pt.api.dto.payment;

import java.math.BigDecimal;

public class CreatePaymentRequest {
    private Long installmentId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private Long operatorId;
    private String providerReference;

    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
}


package ru.pt.api.dto.payment;

public class PaymentStatusUpdateRequest {
    private PaymentStatus status;
    private String providerPayload;

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getProviderPayload() { return providerPayload; }
    public void setProviderPayload(String providerPayload) { this.providerPayload = providerPayload; }
}


package ru.pt.db.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pt_payment")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_payment_seq")
    @SequenceGenerator(name = "pt_payment_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;
    private Long tid;
    private Long installmentId;
    private BigDecimal amount;
    private String currency;
    private String method;
    private String status;
    private Long operatorId;
    private String providerReference;
    private String providerPayload;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTid() { return tid; }
    public void setTid(Long tid) { this.tid = tid; }
    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
    public String getProviderReference() { return providerReference; }
    public void setProviderReference(String providerReference) { this.providerReference = providerReference; }
    public String getProviderPayload() { return providerPayload; }
    public void setProviderPayload(String providerPayload) { this.providerPayload = providerPayload; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}


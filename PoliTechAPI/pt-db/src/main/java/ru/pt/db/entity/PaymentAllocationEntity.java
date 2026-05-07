package ru.pt.db.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pt_payment_allocation")
public class PaymentAllocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pt_payment_allocation_seq")
    @SequenceGenerator(name = "pt_payment_allocation_seq", sequenceName = "pt_seq", allocationSize = 1)
    private Long id;
    private Long tid;
    private Long paymentId;
    private Long installmentId;
    private BigDecimal allocatedAmount;
    private BigDecimal installmentBalanceAfter;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTid() { return tid; }
    public void setTid(Long tid) { this.tid = tid; }
    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public Long getInstallmentId() { return installmentId; }
    public void setInstallmentId(Long installmentId) { this.installmentId = installmentId; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
    public BigDecimal getInstallmentBalanceAfter() { return installmentBalanceAfter; }
    public void setInstallmentBalanceAfter(BigDecimal installmentBalanceAfter) { this.installmentBalanceAfter = installmentBalanceAfter; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}


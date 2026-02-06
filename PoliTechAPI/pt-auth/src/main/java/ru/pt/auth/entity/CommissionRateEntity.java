package ru.pt.auth.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "acc_commission_rates")
public class CommissionRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_commission_rates_seq")
    @SequenceGenerator(name = "acc_commission_rates_seq", sequenceName = "acc_commission_rates_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tid", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "rate_value", precision = 10, scale = 4)
    private BigDecimal rateValue;

    @Column(name = "fixed_amount", precision = 12, scale = 2)
    private BigDecimal fixedAmount;

    @Column(name = "min_amount", precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 12, scale = 2)
    private BigDecimal maxAmount;

    /** Minimum commission rate (0-1) agent can decrease to. */
    @Column(name = "commission_min_rate", precision = 10, scale = 4)
    private BigDecimal commissionMinRate;

    /** Maximum commission rate (0-1) agent can decrease from (base rate). */
    @Column(name = "commission_max_rate", precision = 10, scale = 4)
    private BigDecimal commissionMaxRate;

    @Column(name = "agd_number", length = 100)
    private String agdNumber;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TenantEntity getTenant() { return tenant; }
    public void setTenant(TenantEntity tenant) { this.tenant = tenant; }
    public AccountEntity getAccount() { return account; }
    public void setAccount(AccountEntity account) { this.account = account; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Boolean getDeleted() { return isDeleted; }
    public void setDeleted(Boolean deleted) { isDeleted = deleted; }
    public BigDecimal getRateValue() { return rateValue; }
    public void setRateValue(BigDecimal rateValue) { this.rateValue = rateValue; }
    public BigDecimal getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public BigDecimal getCommissionMinRate() { return commissionMinRate; }
    public void setCommissionMinRate(BigDecimal commissionMinRate) { this.commissionMinRate = commissionMinRate; }
    public BigDecimal getCommissionMaxRate() { return commissionMaxRate; }
    public void setCommissionMaxRate(BigDecimal commissionMaxRate) { this.commissionMaxRate = commissionMaxRate; }
    public String getAgdNumber() { return agdNumber; }
    public void setAgdNumber(String agdNumber) { this.agdNumber = agdNumber; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

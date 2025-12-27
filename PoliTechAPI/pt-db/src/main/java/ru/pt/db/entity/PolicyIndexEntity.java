package ru.pt.db.entity;

import jakarta.persistence.*;
import ru.pt.api.dto.db.PolicyStatus;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity for policy index - provides fast search capabilities
 */
@Entity
@Table(name = "policy_index")
public class PolicyIndexEntity {

    @Id
    @Column(name = "id")
    private UUID policyId;

    @Column(name = "policy_nr", length = 30)
    private String policyNumber;

    @Column(name = "version_no")
    private Integer versionNo;

    @Column(name = "top_version")
    private Boolean topVersion;

    @Column(name = "product_code", length = 30)
    private String productCode;

    @Column(name = "create_date")
    private ZonedDateTime createDate;

    @Column(name = "issue_date")
    private ZonedDateTime issueDate;

    @Column(name = "payment_date")
    private ZonedDateTime paymentDate;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Column(name = "user_account_id")
    private Long userAccountId;

    @Column(name = "client_account_id")
    private Long clientAccountId;

    @Column(name = "version_status", length = 30)
    private String versionStatus;

    @Column(name = "policy_status", length = 20)
    @Enumerated(EnumType.STRING)
    private PolicyStatus policyStatus;

    @Column(name = "payment_order_id", length = 100)
    private String paymentOrderId;

    @Column(name = "ins_company", length = 10)
    private String insCompany;

    @Column(name = "product_version_no", nullable = false)
    private Integer productVersionNo = 1;

    @Column(name = "ph_digest", length = 250)
    private String phDigest;

    @Column(name = "io_digest", length = 250)
    private String ioDigest;

    @Column(name = "user_login", length = 250)
    private String userLogin;

    @Column(name = "premium", precision = 18, scale = 2)
    private java.math.BigDecimal premium;

    @Column(name = "agent_kv_percent", precision = 18, scale = 2)
    private java.math.BigDecimal agentKvPercent;

    @Column(name = "agent_kv_amount", precision = 18, scale = 2)
    private java.math.BigDecimal agentKvAmount;


    // Constructors
    public PolicyIndexEntity() {
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(UUID policyId) {
        this.policyId = policyId;
    }

    // Getters and Setters
    public String getPolicyNr() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public Boolean getTopVersion() {
        return topVersion;
    }

    public void setTopVersion(Boolean topVersion) {
        this.topVersion = topVersion;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(ZonedDateTime createDate) {
        this.createDate = createDate;
    }

    public ZonedDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(ZonedDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public ZonedDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(ZonedDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(Long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public Long getClientAccountId() {
        return clientAccountId;
    }

    public void setClientAccountId(Long clientAccountId) {
        this.clientAccountId = clientAccountId;
    }

    public String getVersionStatus() {
        return versionStatus;
    }

    public void setVersionStatus(String versionStatus) {
        this.versionStatus = versionStatus;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public PolicyStatus getPolicyStatus() {
        return policyStatus;
    }

    public void setPolicyStatus(PolicyStatus policyStatus) {
        this.policyStatus = policyStatus;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public String getInsCompany() {
        return insCompany;
    }

    public void setInsCompany(String insCompany) {
        this.insCompany = insCompany;
    }

    public Integer getProductVersionNo() {
        return productVersionNo;
    }

    public void setProductVersionNo(Integer productVersionNo) {
        this.productVersionNo = productVersionNo;
    }

    public String getPhDigest() {
        return phDigest;
    }

    public void setPhDigest(String phDigest) {
        this.phDigest = phDigest;
    }

    public String getIoDigest() {
        return ioDigest;
    }

    public void setIoDigest(String ioDigest) {
        this.ioDigest = ioDigest;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public java.math.BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(java.math.BigDecimal premium) {
        this.premium = premium;
    }

    public java.math.BigDecimal getAgentKvPercent() {
        return agentKvPercent;
    }

    public void setAgentKvPercent(java.math.BigDecimal agentKvPercent) {
        this.agentKvPercent = agentKvPercent;
    }

    public java.math.BigDecimal getAgentKvAmount() {
        return agentKvAmount;
    }   

    public void setAgentKvAmount(java.math.BigDecimal agentKvAmount) {
        this.agentKvAmount = agentKvAmount;
    }
}
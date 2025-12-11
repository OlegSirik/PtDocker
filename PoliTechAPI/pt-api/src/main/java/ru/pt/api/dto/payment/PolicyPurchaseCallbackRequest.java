package ru.pt.api.dto.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Payload for callbacks triggered after successful policy purchase.
 * Contains enough data to notify the policy holder via email or
 * perform additional business actions.
 */
public class PolicyPurchaseCallbackRequest {

    private String paymentId;
    private String policyNumber;
    private String policyHolderEmail;
    private BigDecimal paidAmount;
    private ZonedDateTime paymentDate;

    public PolicyPurchaseCallbackRequest() {
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getPolicyHolderEmail() {
        return policyHolderEmail;
    }

    public void setPolicyHolderEmail(String policyHolderEmail) {
        this.policyHolderEmail = policyHolderEmail;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public ZonedDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(ZonedDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}


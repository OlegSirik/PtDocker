package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LotAttributes {
    @JsonProperty("ReturnUrl")
    private String returnUrl;
    @JsonProperty("FailUrl")
    private String failUrl;
    @JsonProperty("AgentTypeId")
    private String agentTypeId;
    @JsonProperty("PayerId")
    private String payerId;
    @JsonProperty("PaymentLinkExpirationDate")
    private String paymentLinkExpirationDate;

    public LotAttributes() {
    }

    public LotAttributes(String returnUrl, String failUrl, String agentTypeId, String payerId, String paymentLinkExpirationDate) {
        this.returnUrl = returnUrl;
        this.failUrl = failUrl;
        this.agentTypeId = agentTypeId;
        this.payerId = payerId;
        this.paymentLinkExpirationDate = paymentLinkExpirationDate;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getFailUrl() {
        return failUrl;
    }

    public void setFailUrl(String failUrl) {
        this.failUrl = failUrl;
    }

    public String getAgentTypeId() {
        return agentTypeId;
    }

    public void setAgentTypeId(String agentTypeId) {
        this.agentTypeId = agentTypeId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPaymentLinkExpirationDate() {
        return paymentLinkExpirationDate;
    }

    public void setPaymentLinkExpirationDate(String paymentLinkExpirationDate) {
        this.paymentLinkExpirationDate = paymentLinkExpirationDate;
    }

}

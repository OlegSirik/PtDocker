package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequestAttributes {
    @JsonProperty("PayerTypeId")
    private String payerTypeId;
    @JsonProperty("PayerId")
    private String payerId;
    @JsonProperty("PayerEmail")
    private String payerEmail;
    @JsonProperty("PayerPhone")
    private String payerPhone;
    @JsonProperty("AgentTypeId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String agentTypeId;
    @JsonProperty("AgentId")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String agentId;
    @JsonProperty("SourceId")
    private String sourceId;

    public PaymentRequestAttributes() {
    }

    public PaymentRequestAttributes(String payerTypeId, String payerId, String payerEmail, String payerPhone, String agentTypeId, String agentId, String sourceId) {
        this.payerTypeId = payerTypeId;
        this.payerId = payerId;
        this.payerEmail = payerEmail;
        this.payerPhone = payerPhone;
        this.agentTypeId = agentTypeId;
        this.agentId = agentId;
        this.sourceId = sourceId;
    }

    public String getPayerTypeId() {
        return payerTypeId;
    }

    public void setPayerTypeId(String payerTypeId) {
        this.payerTypeId = payerTypeId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getPayerPhone() {
        return payerPhone;
    }

    public void setPayerPhone(String payerPhone) {
        this.payerPhone = payerPhone;
    }

    public String getAgentTypeId() {
        return agentTypeId;
    }

    public void setAgentTypeId(String agentTypeId) {
        this.agentTypeId = agentTypeId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

}

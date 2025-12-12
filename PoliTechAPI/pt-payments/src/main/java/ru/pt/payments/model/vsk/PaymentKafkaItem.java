package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentKafkaItem {
    @JsonProperty("DocUid")
    private String docUid;
    @JsonProperty("DocNumber")
    private String docNumber;
    @JsonProperty("DocTypeUid")
    private String docTypeUid;
    @JsonProperty("DocStateUid")
    private String docStateUid;
    @JsonProperty("DocPayChannelId")
    private String docPayChannelId;
    @JsonProperty("PaymentMethodId")
    private String paymentMethodId;

    public PaymentKafkaItem() {
    }

    public PaymentKafkaItem(String docUid, String docNumber, String docTypeUid, String docStateUid, String docPayChannelId, String paymentMethodId) {
        this.docUid = docUid;
        this.docNumber = docNumber;
        this.docTypeUid = docTypeUid;
        this.docStateUid = docStateUid;
        this.docPayChannelId = docPayChannelId;
        this.paymentMethodId = paymentMethodId;
    }

    public String getDocUid() {
        return docUid;
    }

    public void setDocUid(String docUid) {
        this.docUid = docUid;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public String getDocTypeUid() {
        return docTypeUid;
    }

    public void setDocTypeUid(String docTypeUid) {
        this.docTypeUid = docTypeUid;
    }

    public String getDocStateUid() {
        return docStateUid;
    }

    public void setDocStateUid(String docStateUid) {
        this.docStateUid = docStateUid;
    }

    public String getDocPayChannelId() {
        return docPayChannelId;
    }

    public void setDocPayChannelId(String docPayChannelId) {
        this.docPayChannelId = docPayChannelId;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}

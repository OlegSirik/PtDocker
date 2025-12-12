package ru.pt.payments.model.vsk;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentKafkaResponse {
    @JsonProperty("OrderId")
    private String orderId;
    @JsonProperty("OrderUid")
    private String orderUid;
    @JsonProperty("OrderState")
    private String orderState;
    @JsonProperty("DocId")
    private String docId;
    @JsonProperty("description")
    private String description;
    @JsonProperty("Description")
    private String description2;
    @JsonProperty("paymentQrDate")
    private String paymentQRData;
    @JsonProperty("ErrorCode")
    private String errorCode;
    @JsonProperty("code")
    private String code;
    @JsonProperty("MessageType")
    private String messageType;
    @JsonProperty("OrderStateUid")
    private String orderStateUid;
    @JsonProperty("paymentLink")
    private String paymentLink;
    @JsonProperty("PayerTypeUid")
    private String payerTypeUid;
    @JsonProperty("PayerEmail")
    private String payerEmail;
    @JsonProperty("PayerPhone")
    private String payerPhone;
    @JsonProperty("OrderNum")
    private String orderNum;
    @JsonProperty("Items")
    private List<PaymentKafkaItem> items;

    public PaymentKafkaResponse() {
    }

    public PaymentKafkaResponse(String orderId, String orderUid, String orderState, String docId, String description, String description2, String paymentQRData, String errorCode, String code, String messageType, String orderStateUid, String paymentLink, String payerTypeUid, String payerEmail, String payerPhone, String orderNum, List<PaymentKafkaItem> items) {
        this.orderId = orderId;
        this.orderUid = orderUid;
        this.orderState = orderState;
        this.docId = docId;
        this.description = description;
        this.description2 = description2;
        this.paymentQRData = paymentQRData;
        this.errorCode = errorCode;
        this.code = code;
        this.messageType = messageType;
        this.orderStateUid = orderStateUid;
        this.paymentLink = paymentLink;
        this.payerTypeUid = payerTypeUid;
        this.payerEmail = payerEmail;
        this.payerPhone = payerPhone;
        this.orderNum = orderNum;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderUid() {
        return orderUid;
    }

    public void setOrderUid(String orderUid) {
        this.orderUid = orderUid;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription2() {
        return description2;
    }

    public void setDescription2(String description2) {
        this.description2 = description2;
    }

    public String getPaymentQRData() {
        return paymentQRData;
    }

    public void setPaymentQRData(String paymentQRData) {
        this.paymentQRData = paymentQRData;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getOrderStateUid() {
        return orderStateUid;
    }

    public void setOrderStateUid(String orderStateUid) {
        this.orderStateUid = orderStateUid;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public String getPayerTypeUid() {
        return payerTypeUid;
    }

    public void setPayerTypeUid(String payerTypeUid) {
        this.payerTypeUid = payerTypeUid;
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

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public List<PaymentKafkaItem> getItems() {
        return items;
    }

    public void setItems(List<PaymentKafkaItem> items) {
        this.items = items;
    }

}

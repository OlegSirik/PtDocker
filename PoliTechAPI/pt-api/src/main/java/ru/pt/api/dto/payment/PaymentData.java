package ru.pt.api.dto.payment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class PaymentData {
    // вход - номер полиса
    private String policyNumber;
    // вход - тип оплаты
    private PaymentType paymentType;
    // вход - номер платежного периода - пока что 1 константа
    private Long installmentNumber;
    // вход - сумма
    private BigDecimal amount;
    // вход - ссылка на редирект после успешной оплате по карте/СБП
    private String successUrl;
    // вход - ссылка на редирект при ошибке оплаты по карте/СБП
    private String failUrl;
    // выход - ссылка на СБП
    private String qrLink;
    // выход - ссылка на платежный шлюз
    private String paymentLink;
    // выход - файл счета при оплате по счету
    private byte[] billBase64;
    // выход - дата оплаты
    private ZonedDateTime paymentDate;
    // выход - идентификатор заявки на оплату
    private String orderId;

    public PaymentData() {
    }

    public PaymentData(String policyNumber, PaymentType paymentType, Long installmentNumber, BigDecimal amount, String successUrl, String failUrl, String qrLink, String paymentLink, byte[] billBase64, ZonedDateTime paymentDate, String orderId) {
        this.policyNumber = policyNumber;
        this.paymentType = paymentType;
        this.installmentNumber = installmentNumber;
        this.amount = amount;
        this.successUrl = successUrl;
        this.failUrl = failUrl;
        this.qrLink = qrLink;
        this.paymentLink = paymentLink;
        this.billBase64 = billBase64;
        this.paymentDate = paymentDate;
        this.orderId = orderId;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public Long getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Long installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getFailUrl() {
        return failUrl;
    }

    public void setFailUrl(String failUrl) {
        this.failUrl = failUrl;
    }

    public String getQrLink() {
        return qrLink;
    }

    public void setQrLink(String qrLink) {
        this.qrLink = qrLink;
    }

    public String getPaymentLink() {
        return paymentLink;
    }

    public void setPaymentLink(String paymentLink) {
        this.paymentLink = paymentLink;
    }

    public byte[] getBillBase64() {
        return billBase64;
    }

    public void setBillBase64(byte[] billBase64) {
        this.billBase64 = billBase64;
    }

    public ZonedDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(ZonedDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}

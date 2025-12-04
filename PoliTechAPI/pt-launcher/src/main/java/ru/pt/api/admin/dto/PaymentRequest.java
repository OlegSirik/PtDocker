package ru.pt.api.admin.dto;

import java.time.ZonedDateTime;

/**
 * Request class for payment
 */
public class PaymentRequest {
    private ZonedDateTime paymentDate;

    public PaymentRequest() {
    }

    public ZonedDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(ZonedDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
}
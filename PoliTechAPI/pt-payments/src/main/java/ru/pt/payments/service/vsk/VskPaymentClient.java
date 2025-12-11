package ru.pt.payments.service.vsk;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.service.payment.PaymentClient;
import ru.pt.payments.service.vsk.api.VskPaymentApi;

import java.math.BigDecimal;
import java.util.List;

@Component
public class VskPaymentClient implements PaymentClient {

    private static final String PAYMENT_GATE = "VSK";

    private final VskPaymentApi vskPaymentApi;

    public VskPaymentClient(VskPaymentApi vskPaymentApi) {
        this.vskPaymentApi = vskPaymentApi;
    }

    @Override
    public String getPaymentGate() {
        return PAYMENT_GATE;
    }

    @Override
    public PaymentData createPayment(PaymentData paymentData) {
        return null;
    }

    @Override
    public void paymentCallback(String policyId) {

    }

    @Override
    public List<ValidationError> validate(String policyNumber, BigDecimal amount) {
        return List.of();
    }
}

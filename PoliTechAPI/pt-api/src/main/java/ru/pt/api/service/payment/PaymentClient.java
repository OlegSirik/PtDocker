package ru.pt.api.service.payment;

import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.payment.PaymentType;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentClient {

    /**
     * Платежный шлюз к которому относится клиент
     * @return идентификатор шлюза
     */
    String getPaymentGate();

    /**
     * Оплатить полис(явно с клиента)
     * @param paymentData данные для оплаты
     * @return данные для оплаты
     */
    PaymentData createPayment(PaymentData paymentData);

    /**
     * Проверить возможность оплаты полиса
     * @param policyNumber номер полиса
     * @param amount сумма к оплате
     * @return список ошибок
     */
    List<ValidationError> validate(String policyNumber, BigDecimal amount);
}

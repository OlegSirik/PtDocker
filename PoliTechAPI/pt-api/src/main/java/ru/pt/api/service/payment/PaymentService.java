package ru.pt.api.service.payment;

import ru.pt.api.dto.payment.CreateInstallmentsRequest;
import ru.pt.api.dto.payment.CreatePaymentRequest;
import ru.pt.api.dto.payment.InstallmentDto;
import ru.pt.api.dto.payment.PaymentDto;
import ru.pt.api.dto.payment.PaymentStatusUpdateRequest;

import java.util.List;

public interface PaymentService {
    List<InstallmentDto> createInstallments(Long tenantId, CreateInstallmentsRequest request);
    List<InstallmentDto> getInstallments(Long tenantId, Long policyId);
    PaymentDto createPayment(Long tenantId, Long accountId, CreatePaymentRequest request);
    PaymentDto updatePaymentStatus(Long tenantId, Long paymentId, PaymentStatusUpdateRequest request);
    void processWebhook(Long tenantId, String providerCode, String payload);
}


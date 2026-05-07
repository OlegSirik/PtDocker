package ru.pt.db.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.payment.*;
import ru.pt.api.service.payment.PaymentService;
import ru.pt.db.entity.PaymentAllocationEntity;
import ru.pt.db.entity.PaymentEntity;
import ru.pt.db.entity.PaymentInstallmentEntity;
import ru.pt.db.repository.PaymentAllocationRepository;
import ru.pt.db.repository.PaymentInstallmentRepository;
import ru.pt.db.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentInstallmentRepository installmentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository allocationRepository;

    public PaymentServiceImpl(PaymentInstallmentRepository installmentRepository,
                              PaymentRepository paymentRepository,
                              PaymentAllocationRepository allocationRepository) {
        this.installmentRepository = installmentRepository;
        this.paymentRepository = paymentRepository;
        this.allocationRepository = allocationRepository;
    }

    @Override
    public List<InstallmentDto> createInstallments(Long tenantId, CreateInstallmentsRequest request) {
        if (request == null || request.getPolicyId() == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new BadRequestException("Invalid installment creation request");
        }
        int count = switch (normalize(request.getInstallmentType())) {
            case "MONTHLY" -> 12;
            case "QUARTERLY" -> 4;
            default -> 1;
        };
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        BigDecimal part = request.getAmount().divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP);
        List<InstallmentDto> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PaymentInstallmentEntity entity = new PaymentInstallmentEntity();
            entity.setTid(tenantId);
            entity.setPolicyId(request.getPolicyId());
            entity.setInstallmentNr(i + 1);
            entity.setDueDate(startDate.plusMonths(i));
            entity.setAmount(part);
            entity.setCurrency(normalizeCurrency(request.getCurrency()));
            entity.setStatus(InstallmentStatus.UNPAID.name());
            result.add(toDto(installmentRepository.save(entity)));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstallmentDto> getInstallments(Long tenantId, Long policyId) {
        return installmentRepository.findByTidAndPolicyIdOrderByInstallmentNrAsc(tenantId, policyId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public PaymentDto createPayment(Long tenantId, Long accountId, CreatePaymentRequest request) {
        if (request == null || request.getInstallmentId() == null || request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new BadRequestException("Invalid payment request");
        }
        PaymentInstallmentEntity installment = installmentRepository.findById(request.getInstallmentId())
                .filter(i -> Objects.equals(tenantId, i.getTid()))
                .orElseThrow(() -> new NotFoundException("Installment not found"));
        PaymentEntity entity = new PaymentEntity();
        entity.setTid(tenantId);
        entity.setInstallmentId(installment.getId());
        entity.setAmount(request.getAmount());
        entity.setCurrency(normalizeCurrency(request.getCurrency() != null ? request.getCurrency() : installment.getCurrency()));
        entity.setMethod(request.getMethod() == null ? PaymentMethod.CARD.name() : request.getMethod().name());
        entity.setStatus(PaymentStatus.INITIATED.name());
        entity.setProviderReference(request.getProviderReference());
        entity.setOperatorId(resolveOperatorId(accountId, request));
        return toDto(paymentRepository.save(entity));
    }

    @Override
    public PaymentDto updatePaymentStatus(Long tenantId, Long paymentId, PaymentStatusUpdateRequest request) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .filter(p -> Objects.equals(tenantId, p.getTid()))
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        if (request == null || request.getStatus() == null) {
            throw new BadRequestException("Payment status is required");
        }
        payment.setStatus(request.getStatus().name());
        payment.setProviderPayload(request.getProviderPayload());
        if (request.getStatus() == PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
            allocatePayment(tenantId, payment);
        }
        return toDto(paymentRepository.save(payment));
    }

    @Override
    public void processWebhook(Long tenantId, String providerCode, String payload) {
        // Format of bank callback is not finalized yet.
        // Keep method as integration point in service contract.
    }

    private void allocatePayment(Long tenantId, PaymentEntity payment) {
        PaymentInstallmentEntity firstInstallment = installmentRepository.findById(payment.getInstallmentId())
                .filter(i -> Objects.equals(tenantId, i.getTid()))
                .orElseThrow(() -> new NotFoundException("Installment not found"));
        List<PaymentInstallmentEntity> installments = installmentRepository
                .findByTidAndPolicyIdOrderByInstallmentNrAsc(tenantId, firstInstallment.getPolicyId());
        BigDecimal remaining = payment.getAmount();
        for (PaymentInstallmentEntity installment : installments) {
            if (installment.getInstallmentNr() < firstInstallment.getInstallmentNr()) {
                continue;
            }
            BigDecimal allocated = allocationRepository.getAllocatedAmount(tenantId, installment.getId());
            BigDecimal due = installment.getAmount().subtract(allocated);
            if (due.signum() <= 0) {
                installment.setStatus(InstallmentStatus.PAID.name());
                installmentRepository.save(installment);
                continue;
            }
            if (remaining.signum() <= 0) {
                break;
            }
            BigDecimal chunk = due.min(remaining);
            PaymentAllocationEntity allocation = new PaymentAllocationEntity();
            allocation.setTid(tenantId);
            allocation.setPaymentId(payment.getId());
            allocation.setInstallmentId(installment.getId());
            allocation.setAllocatedAmount(chunk);
            BigDecimal balance = due.subtract(chunk);
            allocation.setInstallmentBalanceAfter(balance);
            allocationRepository.save(allocation);
            installment.setStatus(balance.signum() == 0 ? InstallmentStatus.PAID.name() : InstallmentStatus.PARTIAL.name());
            installmentRepository.save(installment);
            remaining = remaining.subtract(chunk);
        }
    }

    private Long resolveOperatorId(Long accountId, CreatePaymentRequest request) {
        if (request.getMethod() == PaymentMethod.CASH && request.getOperatorId() == null) {
            return accountId;
        }
        return request.getOperatorId();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeCurrency(String currency) {
        String normalized = normalize(currency);
        return normalized.isEmpty() ? "RUB" : normalized;
    }

    private InstallmentDto toDto(PaymentInstallmentEntity entity) {
        InstallmentDto dto = new InstallmentDto();
        dto.setId(entity.getId());
        dto.setPolicyId(entity.getPolicyId());
        dto.setInstallmentNr(entity.getInstallmentNr());
        dto.setDueDate(entity.getDueDate());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setStatus(InstallmentStatus.valueOf(entity.getStatus()));
        return dto;
    }

    private PaymentDto toDto(PaymentEntity entity) {
        PaymentDto dto = new PaymentDto();
        dto.setId(entity.getId());
        dto.setInstallmentId(entity.getInstallmentId());
        dto.setAmount(entity.getAmount());
        dto.setCurrency(entity.getCurrency());
        dto.setMethod(PaymentMethod.valueOf(entity.getMethod()));
        dto.setStatus(PaymentStatus.valueOf(entity.getStatus()));
        dto.setOperatorId(entity.getOperatorId());
        dto.setProviderReference(entity.getProviderReference());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setPaidAt(entity.getPaidAt());
        return dto;
    }
}


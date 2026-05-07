package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.dto.payment.CreateInstallmentsRequest;
import ru.pt.api.dto.payment.CreatePaymentRequest;
import ru.pt.api.dto.payment.InstallmentDto;
import ru.pt.api.dto.payment.PaymentDto;
import ru.pt.api.dto.payment.PaymentStatusUpdateRequest;
import ru.pt.api.service.payment.PaymentService;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/payments")
public class PaymentsController {
    private final PaymentService paymentService;
    private final SecurityContextHelper securityContextHelper;

    public PaymentsController(PaymentService paymentService, SecurityContextHelper securityContextHelper) {
        this.paymentService = paymentService;
        this.securityContextHelper = securityContextHelper;
    }

    @PostMapping("/installments/generate")
    public ResponseEntity<List<InstallmentDto>> generateInstallments(@RequestBody CreateInstallmentsRequest request) {
        Long tenantId = currentTenantId();
        return ResponseEntity.ok(paymentService.createInstallments(tenantId, request));
    }

    @GetMapping("/installments")
    public ResponseEntity<List<InstallmentDto>> getInstallments(@RequestParam Long policyId) {
        Long tenantId = currentTenantId();
        return ResponseEntity.ok(paymentService.getInstallments(tenantId, policyId));
    }

    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(@RequestBody CreatePaymentRequest request) {
        var user = securityContextHelper.getCurrentUserOrThrow();
        return ResponseEntity.ok(paymentService.createPayment(user.getTenantId(), user.getAccountId(), request));
    }

    @PostMapping("/{paymentId}/status")
    public ResponseEntity<PaymentDto> updateStatus(@PathVariable Long paymentId,
                                                   @RequestBody PaymentStatusUpdateRequest request) {
        Long tenantId = currentTenantId();
        return ResponseEntity.ok(paymentService.updatePaymentStatus(tenantId, paymentId, request));
    }

    private Long currentTenantId() {
        return securityContextHelper.getAuthenticatedUser()
                .map(user -> user.getTenantId())
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }
}


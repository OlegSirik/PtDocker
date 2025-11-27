package ru.pt.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Only for storage operations! Assumes no additional business logic
 * Требует аутентификации для всех операций
 *
 * URL Pattern: /api/v1/{tenantCode}/sales/policies
 * tenantCode: pt, vsk, msg
 * domain: sales
 * resource: policies
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/sales/policies")
public class DbController extends SecuredController {

    private final ProcessOrchestrator processOrchestrator;

    public DbController(ProcessOrchestrator processOrchestrator, SecurityContextHelper securityContextHelper) {
        super(securityContextHelper);
        this.processOrchestrator = processOrchestrator;
    }


    /**
     * Create a new policy
     * POST /api/v1/{tenantCode}/sales/policies
     * Требуется право POLICY на продукт
     */
    @PostMapping
    public ResponseEntity<PolicyData> createPolicy(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody String request) {
        requireAuthenticated(user);
        // TODO: Извлечь productCode из request и проверить права
        // requireProductPolicy(user, productCode);
        return ResponseEntity.ok(processOrchestrator.createPolicy(request));
    }

    /**
     * Update an existing policy
     * PUT /api/v1/{tenantCode}/sales/policies/{policyNumber}
     * Требуется право ADDENDUM на продукт
     */
    @PutMapping("/{policyNumber}")
    public ResponseEntity<PolicyData> updatePolicy(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("policyNumber") String policyNumber,
            @RequestBody String request) {
        requireAuthenticated(user);
        // TODO: Извлечь productCode из существующей политики и проверить права
        // requireProductWrite(user, productCode);
        PolicyData updated = processOrchestrator.updatePolicy(policyNumber, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get policy by ID
     * GET /api/v1/{tenantCode}/sales/policies/{policyId}
     * Требуется право READ на продукт
     */
    @GetMapping("/{policyId}")
    public ResponseEntity<PolicyData> getPolicyById(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("policyId") UUID policyId) {
        requireAuthenticated(user);
        PolicyData policy = processOrchestrator.getPolicyById(policyId);
        return ResponseEntity.ok(policy);
    }

    /**
     * Get policy by policy number
     * GET /api/v1/{tenantCode}/sales/policies/by-number/{policyNumber}
     */
    @GetMapping("/by-number/{policyNumber}")
    public ResponseEntity<PolicyData> getPolicyByNumber(
            @PathVariable String tenantCode,
            @PathVariable("policyNumber") String policyNumber) {
        PolicyData policy = processOrchestrator.getPolicyByNumber(policyNumber);
        return ResponseEntity.ok(policy);
    }

    /**
     * Get all policies by user account ID
     * GET /api/v1/{tenantCode}/sales/policies/by-account/{userAccountId}
     */
    @GetMapping("/by-account/{userAccountId}")
    public ResponseEntity<List<PolicyData>> getPoliciesByUserAccountId(
            @PathVariable String tenantCode,
            @PathVariable("userAccountId") Long userAccountId) {
        // List<PolicyData> policies = policyDataService.getPoliciesByUserAccountId(userAccountId);
        return ResponseEntity.ok(null);
    }

    /**
     * Mark policy as paid
     * POST /api/v1/{tenantCode}/sales/policies/{policyNumber}/paid
     */
    @PostMapping("/{policyNumber}/paid")
    public ResponseEntity<Void> markPolicyAsPaid(
            @PathVariable String tenantId,
            @PathVariable("policyNumber") String policyNumber,
            @RequestBody PaymentRequest request) {
        ZonedDateTime paymentDate = request.getPaymentDate() != null
            ? request.getPaymentDate() 
            : ZonedDateTime.now();
        
        // policyDataService.policyStatusPaid(policyNumber, paymentDate);
        return ResponseEntity.ok().build();
    }


    /**
     * Request class for payment
     */
    public static class PaymentRequest {
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
}


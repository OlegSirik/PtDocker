package ru.pt.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.UserDetailsImpl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Only for storage operations! Assumes no additional business logic
 * Требует аутентификации для всех операций
 */
@RestController
@RequestMapping("/db")
public class DbController extends SecuredController {

    private final ProcessOrchestrator processOrchestrator;

    public DbController(ProcessOrchestrator processOrchestrator) {
        this.processOrchestrator = processOrchestrator;
    }


    /**
     * Create a new policy
     * POST /sales/policies
     * Требуется право POLICY на продукт
     */
    @PostMapping("/policies")
    public ResponseEntity<PolicyData> createPolicy(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody String request) {
        requireAuthenticated(user);
        // TODO: Извлечь productCode из request и проверить права
        // requireProductPolicy(user, productCode);
        return ResponseEntity.ok(processOrchestrator.createPolicy(request));
    }

    /**
     * Update an existing policy
     * PUT /sales/policies/{id}
     * Требуется право ADDENDUM на продукт
     */
    @PutMapping("/policies/{policyNumber}")
    public ResponseEntity<PolicyData> updatePolicy(
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
     * GET /sales/policies/{id}
     * Требуется право READ на продукт
     */
    @GetMapping("/policies/{id}")
    public ResponseEntity<PolicyData> getPolicyById(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") UUID id) {
        requireAuthenticated(user);
        PolicyData policy = processOrchestrator.getPolicyById(id);
        return ResponseEntity.ok(policy);
    }

    /**
     * Get policy by policy number
     * GET /sales/policies/by-number/{policyNumber}
     */
    @GetMapping("/policies/by-number/{policyNumber}")
    public ResponseEntity<PolicyData> getPolicyByNumber(@PathVariable("policyNumber") String policyNumber) {
        PolicyData policy = processOrchestrator.getPolicyByNumber(policyNumber);
        return ResponseEntity.ok(policy);
    }

    /**
     * Get all policies by user account ID
     * GET /sales/policies/by-account/{userAccountId}
     */
    // TODO securityContext and token
    @GetMapping("/policies/by-account/{userAccountId}")
    public ResponseEntity<List<PolicyData>> getPoliciesByUserAccountId(@PathVariable("userAccountId") Long userAccountId) {
        // List<PolicyData> policies = policyDataService.getPoliciesByUserAccountId(userAccountId);
        return ResponseEntity.ok(null);
    }

    /**
     * Mark policy as paid
     * POST /sales/policies/{policyNumber}/paid
     * TODO не тут должно быть + не та dto
     */
    @PostMapping("/policies/{policyNumber}/paid")
    public ResponseEntity<Void> markPolicyAsPaid(@PathVariable("policyNumber") String policyNumber,
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


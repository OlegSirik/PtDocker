package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.PaymentRequest;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.process.FileProcessService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.db.service.DbStorageService;

import java.time.ZonedDateTime;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Only for storage operations! Assumes no additional business logic
 * Требует аутентификации для всех операций
 * <p>
 * URL Pattern: /api/v1/{tenantCode}/sales/policies
 * tenantCode: pt, vsk, msg
 * domain: sales
 * resource: policies
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/sales")
public class SalesController extends SecuredController {

    private final ProcessOrchestrator processOrchestrator;
    private final DbStorageService dbStorageService;
    private final FileProcessService fileProcessService;

    public SalesController(ProcessOrchestrator processOrchestrator,
                           SecurityContextHelper securityContextHelper,
                           DbStorageService dbStorageService,
                           FileProcessService fileProcessService
    ) {
        super(securityContextHelper);
        this.processOrchestrator = processOrchestrator;
        this.dbStorageService = dbStorageService;
        this.fileProcessService = fileProcessService;
    }

    /**
     * Update an existing policy
     * PUT /api/v1/{tenantCode}/sales/policies/{policyNumber}
     * Требуется право ADDENDUM на продукт
     */
    @PutMapping("/policies/{policyNumber}")
    public ResponseEntity<PolicyData> updatePolicy(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("policyNumber") String policyNumber,
            @RequestBody String request) {
        requireAuthenticated(user);
        // TODO: Извлечь productCode из существующей политики и проверить права
//         requireProductWrite(user, productCode);
        PolicyData updated = processOrchestrator.updatePolicy(policyNumber, request);
        return ResponseEntity.ok(updated);
    }


    /**
     * Get policy by ID
     * GET /api/v1/{tenantCode}/sales/policies
     * Требуется право READ на продукт
     */
    @GetMapping
    public ResponseEntity<List<PolicyData>> getPolicies(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        requireAuthenticated(user);
        return ResponseEntity.ok(dbStorageService.getPoliciesForUser());
    }

    /**
     * Get policy by policy number
     * GET /api/v1/{tenantCode}/sales/policies/{policyNumber}
     */
    @GetMapping("/{policyNumber}")
    public ResponseEntity<PolicyData> getPolicyByNumber(
            @PathVariable String tenantCode,
            @PathVariable("policyNumber") String policyNumber) {
        PolicyData policy = processOrchestrator.getPolicyByNumber(policyNumber);
        return ResponseEntity.ok(policy);
    }

    /**
     * Mark policy as paid
     * POST /api/v1/{tenantCode}/sales/policies/{policyNumber}/paid
     */
    @PostMapping("/{policyNumber}/paid")
    public ResponseEntity<Void> markPolicyAsPaid(
            @PathVariable String tenantCode,
            @PathVariable("policyNumber") String policyNumber,
            @RequestBody PaymentRequest request) {
        ZonedDateTime paymentDate = request.getPaymentDate() != null
                ? request.getPaymentDate()
                : ZonedDateTime.now();

        // policyDataService.policyStatusPaid(policyNumber, paymentDate);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/quotes")
    public ResponseEntity<String> quoteValidator(
            @PathVariable("tenantCode") String tenantCode,
            @RequestBody String requestBody) {
        String result = processOrchestrator.calculate(requestBody);
        return ResponseEntity.ok().contentType(APPLICATION_JSON).body(result);
    }

    @PostMapping(value = "/policies")
    public ResponseEntity<String> saveValidator(
            @PathVariable("tenantCode") String tenantCode,
            @RequestBody String requestBody) {
        String result = processOrchestrator.save(requestBody);
        return ResponseEntity.ok().contentType(APPLICATION_JSON).body(result);
    }

    @PostMapping("/policies/{policy-nr}/printpf/{pf-type}")
    public byte[] printPolicy(
            @PathVariable("policy-nr") String policyNr,
            @PathVariable("pf-type") String pfType,
            @PathVariable("tenantCode") String tenantCode) {
        return fileProcessService.generatePrintForm(policyNr, pfType);
    }


}

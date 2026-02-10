package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import ru.pt.api.admin.dto.PaymentRequest;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.sales.QuoteDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.db.StorageService;
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

    private static final Logger logger = LoggerFactory.getLogger(SalesController.class);

    private final ProcessOrchestrator processOrchestrator;
    private final StorageService dbStorageService;
    private final FileProcessService fileProcessService;

    public SalesController(ProcessOrchestrator processOrchestrator,
                           SecurityContextHelper securityContextHelper,
                           StorageService dbStorageService,
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
        
        // Validate policyNumber
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("policyNumber"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "policyNumber"
            );
            throw new BadRequestException(errorModel);
        }
        
        // Validate request body
        if (request == null || request.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("request body"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "requestBody"
            );
            throw new BadRequestException(errorModel);
        }
        
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
        
        // Validate policyNumber
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("policyNumber"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "policyNumber"
            );
            throw new BadRequestException(errorModel);
        }
        
        PolicyData policy = processOrchestrator.getPolicyByNumber(policyNumber);
        if (policy == null) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                404,
                ErrorConstants.policyNotFound(policyNumber),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_NOT_FOUND,
                "policyNumber"
            );
            throw new NotFoundException(errorModel);
        }
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
        
        // Validate policyNumber
        if (policyNumber == null || policyNumber.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("policyNumber"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "policyNumber"
            );
            throw new BadRequestException(errorModel);
        }
        
        // Validate request
        if (request == null) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("request body"),
                ErrorConstants.DOMAIN_PAYMENT,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "requestBody"
            );
            throw new BadRequestException(errorModel);
        }
        
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
        
        // Validate request body
        if (requestBody == null || requestBody.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("request body"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "requestBody"
            );
            throw new BadRequestException(errorModel);
        }
        
        String result = processOrchestrator.calculate(requestBody);
        return ResponseEntity.ok().contentType(APPLICATION_JSON).body(result);
    }


    @GetMapping("/quotes")
    public ResponseEntity<List<QuoteDto>> getAccountQuotes(
            @RequestParam(value = "qstr", required = false) String qstr,
            @PathVariable("tenantCode") String tenantCode) {
        String searchQuery = (qstr != null && !qstr.trim().isEmpty()) ? qstr.trim() : "";
        List<QuoteDto> quotes = dbStorageService.getAccountQuotes(searchQuery);
        return ResponseEntity.ok().body(quotes);
    }

    @PostMapping(value = "/policies")
    public ResponseEntity<String> saveValidator(
            @PathVariable("tenantCode") String tenantCode,
            @RequestBody String requestBody) {
        
        // Validate request body
        if (requestBody == null || requestBody.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("request body"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "requestBody"
            );
            throw new BadRequestException(errorModel);
        }
        
        String result = processOrchestrator.save(requestBody);
        return ResponseEntity.ok().contentType(APPLICATION_JSON).body(result);
    }

    @GetMapping("/policies/{policy-nr}/printpf/{pf-type}")
    public byte[] printPolicy(
            @PathVariable("policy-nr") String policyNr,
            @PathVariable("pf-type") String pfType,
            @PathVariable("tenantCode") String tenantCode) {
        
        // Validate policy number
        if (policyNr == null || policyNr.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("policy-nr"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "policy-nr"
            );
            throw new BadRequestException(errorModel);
        }
        
        // Validate print form type
        if (pfType == null || pfType.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("pf-type"),
                ErrorConstants.DOMAIN_FILE,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "pf-type"
            );
            throw new BadRequestException(errorModel);
        }
        
        return fileProcessService.generatePrintForm(policyNr, pfType);
    }

}

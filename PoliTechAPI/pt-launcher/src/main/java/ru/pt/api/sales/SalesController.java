package ru.pt.api.sales;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.admin.dto.PaymentRequest;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.db.repository.PolicyRepository;
import ru.pt.db.service.DbStorageService;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private final PolicyIndexRepository policyIndexRepository;
    private final PolicyRepository policyRepository;
    private final ProductVersionRepository productVersionRepository;
    private final ProductRepository productRepository;
    private final FileService fileService;
    private final PreProcessService preProcessService;
    private final DbStorageService dbStorageService;

    public SalesController(ProcessOrchestrator processOrchestrator, SecurityContextHelper securityContextHelper,
                        PolicyIndexRepository policyIndexRepository, PolicyRepository policyRepository,
                        ProductVersionRepository productVersionRepository, ProductRepository productRepository,
                        FileService fileService, PreProcessService preProcessService, DbStorageService dbStorageService
    ) {
        super(securityContextHelper);
        this.processOrchestrator = processOrchestrator;
        this.policyIndexRepository = policyIndexRepository;
        this.policyRepository = policyRepository;
        this.productVersionRepository = productVersionRepository;
        this.productRepository = productRepository;
        this.fileService = fileService;
        this.preProcessService = preProcessService;
        this.dbStorageService = dbStorageService;
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
            @PathVariable("tenantCode") String tenantCode) throws JsonProcessingException {

        var policyIndex = policyIndexRepository
                .findByPolicyNumber(policyNr)
                .orElseThrow(() ->
                        new NotFoundException("Не удалось найти полис по номеру - %s".formatted(policyNr))
                );
        var policy = policyRepository.findById(policyIndex.getPolicyId())
                .orElseThrow(() ->
                        new NotFoundException("Не удалось найти полис по id - %s".formatted(policyIndex.getPolicyId()))
                );
        var productId = productRepository.findByCodeAndIsDeletedFalse(policyIndex.getProductCode())
                .orElseThrow(() ->
                        new NotFoundException(
                                "Не удалось найти продукт; productCode %s".formatted(policyIndex.getProductCode())
                        )
                ).getId();

        var productVersion = productVersionRepository.findByProductIdAndVersionNo(
                productId, policyIndex.getVersionNo()
        ).orElseThrow(() ->
                {
                    logger.error(
                            "Не удалось найти версию продукта; productId {}, versionNo {}",
                            productId, policyIndex.getVersionNo()
                    );
//                return new InternalServerErrorException("Обратитесь к администратору");
                    return new InternalServerErrorException(
                            "Не удалось найти версию продукта; productId %s, versionNo %s".formatted(
                                    productId, policyIndex.getVersionNo())
                    );
                }
        );

        var json = productVersion.getProduct();
        ArrayNode context = (ArrayNode) new ObjectMapper().readTree(json).get("vars");

        var lobModel = new LobModel();
        List<LobVar> vars = new LinkedList<>();

        for (int i = 0; i < context.size(); i++) {
            JsonNode val = context.get(i);
            try {
                vars.add(new ObjectMapper().readValue(val.toString(), LobVar.class));
            } catch (JsonProcessingException e) {
                logger.error("Не удалось спарcить productVersion.product.vars[{}]", i);
//                throw new InternalServerErrorException("Обратитесь к администратору");
                throw new InternalServerErrorException(
                        "Не удалось спарcить productVersion.product.vars[%s]".formatted(i)
                );
            }
        }

        lobModel.setMpVars(vars);
        vars.forEach(it -> it.setVarValue(null));
        vars = preProcessService.evaluateAndEnrichVariables(policy.getPolicy(), lobModel, productVersion.getProduct());
        Map<String, String> keyValues = new HashMap<>();

        for (LobVar node : vars) {
            String key = node.getVarCode();
            String value = node.getVarValue();
            System.out.println(key + " " + value);
            keyValues.put(key, value);
        }
        return fileService.getFile(pfType, keyValues);
    }



}

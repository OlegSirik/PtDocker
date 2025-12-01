package ru.pt.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.db.repository.PolicyRepository;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;

import java.util.*;


/**
 * Test контроллер для разработки и отладки
 * <p>
 * URL Pattern: /api/v1/{tenantCode}/test/*
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenant-code}/test")
public class Test extends SecuredController {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    private final AccountService accountService;
    private final ProcessOrchestrator processOrchestrator;
    private final PolicyIndexRepository policyIndexRepository;
    private final PolicyRepository policyRepository;
    private final ProductVersionRepository productVersionRepository;
    private final ProductRepository productRepository;
    private final FileService fileService;
    private final PreProcessService preProcessService;

    protected Test(
        SecurityContextHelper securityContextHelper, AccountService accountService,
        ProcessOrchestrator processOrchestrator, PolicyIndexRepository policyIndexRepository, PolicyRepository policyRepository,
        ProductVersionRepository productVersionRepository, ProductRepository productRepository,
        FileService fileService, PreProcessService preProcessService
    ) {
        super(securityContextHelper);
        this.accountService = accountService;
        this.processOrchestrator = processOrchestrator;
        this.policyIndexRepository = policyIndexRepository;
        this.policyRepository = policyRepository;
        this.productVersionRepository = productVersionRepository;
        this.productRepository = productRepository;
        this.fileService = fileService;
        this.preProcessService = preProcessService;
    }

    @PostMapping("/create-client")
    public ResponseEntity<Account> createClient(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createClient(account.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-group")
    public ResponseEntity<Account> createGroup(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createGroup(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-account")
    public ResponseEntity<Account> createAccount(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createAccount(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }


    @PostMapping("/create-subaccount")
    public ResponseEntity<Account> createSubaccount(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createSubaccount(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-product-roles/{accountId}")
    public ResponseEntity<Object> getProductRoles(
            @PathVariable("tenant-code") String tenantCode,
            @PathVariable("accountId") long accountId) {
        Set<String> result = accountService.getProductRoles(accountId);
        return ResponseEntity.ok(result.toArray());
    }

    @GetMapping("/get-account-login")
    public ResponseEntity<ObjectNode> getAccountLogin(
            @PathVariable("tenant-code") String tenantCode,
            @RequestHeader("login") String login,
            @RequestHeader("client") String client,
            @RequestHeader(value = "accountNr", required = false) Long accountNr) {

        ObjectNode result = accountService.getAccountLogin(login, client, accountNr);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/quote/validator")
    public ResponseEntity<String> quoteValidator(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody String requestBody) {
        String result = processOrchestrator.calculate(requestBody);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/policy/validator")
    public ResponseEntity<String> saveValidator(
            @PathVariable("tenant-code") String tenantCode,
            @RequestBody String requestBody) {
        String result = processOrchestrator.save(requestBody);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/policy/printpf/{policy-nr}/{pf-type}")
    public byte[] printPolicy(
        @PathVariable("policy-nr") String policyNr,
        @PathVariable("pf-type") String pfType,
        @PathVariable("tenant-code") String tenantCode) throws JsonProcessingException {
        
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
                vars.add(new ObjectMapper().readValue(val.asText(), LobVar.class));
            } catch (JsonProcessingException e) {
                logger.error("Не удалось спарcить productVersion.product.vars[{}]", i);
//                throw new InternalServerErrorException("Обратитесь к администратору");
                throw new InternalServerErrorException(
                    "Не удалось спарcить productVersion.product.vars[%s]".formatted(i)
                );
            }
        }

        lobModel.setMpVars(vars);
        vars = preProcessService.evaluateAndEnrichVariables(policy.getPolicy(), lobModel, productVersion.getProduct());
        Map<String, String> keyValues = new HashMap<>();

        for (LobVar node : vars) {
            String key = node.getVarCode();
            String value = node.getVarValue();
            if (StringUtils.isBlank(value)) {
                value = node.getVarPath();
            }
            System.out.println(key + " " + value);
            keyValues.put(key, value);
        }
        return fileService.getFile(pfType, keyValues);
    }
}

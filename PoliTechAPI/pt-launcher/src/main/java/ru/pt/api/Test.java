package ru.pt.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.file.FileService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.db.repository.PolicyIndexRepository;
import ru.pt.exception.InternalServerErrorException;
import ru.pt.product.repository.ProductRepository;
import ru.pt.product.repository.ProductVersionRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/test")
public class Test extends SecuredController {

    private static final Logger logger = LoggerFactory.getLogger(Test.class);

    private final AccountService accountService;
    private final ProcessOrchestrator processOrchestrator;
    private final PolicyIndexRepository policyIndexRepository;
    private final ProductVersionRepository productVersionRepository;
    private final ProductRepository productRepository;
    private final FileService fileService;

    protected Test(
        SecurityContextHelper securityContextHelper, AccountService accountService,
        ProcessOrchestrator processOrchestrator, PolicyIndexRepository policyIndexRepository,
        ProductVersionRepository productVersionRepository, ProductRepository productRepository,
        FileService fileService
    ) {
        super(securityContextHelper);
        this.accountService = accountService;
        this.processOrchestrator = processOrchestrator;
        this.policyIndexRepository = policyIndexRepository;
        this.productVersionRepository = productVersionRepository;
        this.productRepository = productRepository;
        this.fileService = fileService;
    }

    @PostMapping("/create-client")
    public ResponseEntity<Account> createClient(@RequestBody Account account) {
        // Replace Object with the actual DTO class for createClient if available
        Account result = accountService.createClient(account.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-group")
    public ResponseEntity<Account> createGroup(@RequestBody Account account) {
        Account result = accountService.createGroup(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-account")
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        Account result = accountService.createAccount(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }


    @PostMapping("/create-subaccount")
    public ResponseEntity<Account> createSubaccount(@RequestBody Account account) {
        Account result = accountService.createSubaccount(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/get-product-roles/{accountId}")
    public ResponseEntity<Object> getProductRoles(@PathVariable("accountId") long accountId) {
        Set<String> result = accountService.getProductRoles(accountId);
        return ResponseEntity.ok(result.toArray());
    }

    @GetMapping("/get-account-login")
    public ResponseEntity<ObjectNode> getAccountLogin(
        @RequestHeader("login") String login,
        @RequestHeader("client") String client,
        @RequestHeader(value = "accountNr", required = false) Long accountNr) {

        ObjectNode result = accountService.getAccountLogin(login, client,accountNr);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/quote/validator")
    public ResponseEntity<String> quoteValidator(@RequestBody String requestBody) {
        String result = processOrchestrator.calculate(requestBody);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/policy/validator")
    public ResponseEntity<String> saveValidator(@RequestBody String requestBody) {
        String result = processOrchestrator.save(requestBody);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/policy/printpf/{policy-nr}/{pf-type}")
    public byte[] printPolicy(
        @PathVariable("policy-nr") String policyNr, @PathVariable("pf-type") String pfType
    ) throws JsonProcessingException {
        var policyIndex = policyIndexRepository
            .findByPolicyNumber(policyNr)
            .orElseThrow(() ->
                new BadRequestException("Не удалось найти полис по номеру - %s".formatted(policyNr))
            );

        var productId = productRepository.findByCodeAndIsDeletedFalse(policyIndex.getProductCode())
            .orElseThrow(() ->
                {
                    logger.error(
                        "Не удалось найти продукт; productCode {}",
                        policyIndex.getProductCode()
                    );
                    return new NotFoundException("Внутрення ошибка, обратитесь к администратору");
                }
            ).getId();

        var productVersion = productVersionRepository.findByProductIdAndVersionNo(
            productId, policyIndex.getVersionNo()
        ).orElseThrow(() ->
            {
                logger.error(
                    "Не удалось найти версию продукта; productCode {}, versionNo {}",
                    policyNr, policyIndex.getVersionNo()
                );
                return new InternalServerErrorException("Обратитесь к администратору");
            }
        );

        var json = productVersion.getProduct();

        ArrayNode context = (ArrayNode) new ObjectMapper().readTree(json).get("vars");
        Map<String, String> keyValues = new HashMap<>();

        for (JsonNode node : context) {
            String key = node.get("varCode").asText();
            String value = node.get("varValue").asText();
            if (StringUtils.isBlank(value)) {
                value = node.get("varPath").asText();
            }
            System.out.println(key + " " + value);
            keyValues.put(key, value);
        }

        return fileService.getFile(pfType, keyValues);
    }
}

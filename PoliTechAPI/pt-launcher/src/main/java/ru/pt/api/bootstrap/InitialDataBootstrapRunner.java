package ru.pt.api.bootstrap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.calculator.CoefficientDataRow;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.calculator.CoefficientService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.product.ProductTestService;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.auth.security.UserDetailsImpl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Component
public class InitialDataBootstrapRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(InitialDataBootstrapRunner.class);

    private static final Long TENANT_ID = 10L;
    private static final Long ACCOUNT_ID = 16L;

    private static final List<String> LOB_FILES = List.of(
            "classpath:bootstrap/ACC/business-line_ACC.json",
            "classpath:bootstrap/CRD/business-line.json",
            "classpath:bootstrap/GAD/business-line.json",
            "classpath:bootstrap/PAX/business-line_PAX.json",
            "classpath:bootstrap/CRG/business-line_CRG.json"
    );
    private static final List<String> PRODUCT_FILES = List.of(
            "classpath:bootstrap/ACC/product_НС.json",
            "classpath:bootstrap/ACC/NS_CLASSIC/product_NS_CLASSIC.json",
            "classpath:bootstrap/GAD/product_COMPLEX_PROTECTION.json",
            "classpath:bootstrap/CRG/product_CARGO_GENERAL.json"
    );

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final LobService lobService;
    private final ProductService productService;
    private final CalculatorService calculatorService;
    private final CoefficientService coefficientService;
    private final ProductTestService productTestService;

    public InitialDataBootstrapRunner(ObjectMapper objectMapper,
                                      ResourceLoader resourceLoader,
                                      LobService lobService,
                                      ProductService productService,
                                      CalculatorService calculatorService,
                                      CoefficientService coefficientService,
                                      ProductTestService productTestService) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.lobService = lobService;
        this.productService = productService;
        this.calculatorService = calculatorService;
        this.coefficientService = coefficientService;
        this.productTestService = productTestService;
    }

    Map<String, Long> calculatorIdMap = new HashMap<>();
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        runAsSystem(() -> {
            loadLobs(LOB_FILES);
            loadProducts(PRODUCT_FILES);

            Long calculatorId = null;

            
            //loadCoefficients(calculatorId, "K_Age", "classpath:bootstrap/ACC/coefficients_K_Age.csv");

            calculatorId = loadCalculators("classpath:bootstrap/ACC/calculator__HC_0.json");
            calculatorId = loadCalculators("classpath:bootstrap/ACC/calculator__HC_1.json");
            calculatorId = loadCalculators("classpath:bootstrap/ACC/calculator__HC_2.json");
            calculatorId = loadCalculators("classpath:bootstrap/ACC/calculator__HC_3.json");
            calculatorId = loadCalculators("classpath:bootstrap/ACC/calculator__HC_4.json");
            calculatorId = loadCalculators("classpath:bootstrap/ACC/calculator__HC_5.json");
            calculatorId = loadCalculators("classpath:bootstrap/ACC/NS_CLASSIC/calculator__NS_CLASSIC.json");

            loadCoefficients(calculatorId, "K_sport", "classpath:bootstrap/ACC/NS_CLASSIC/coefficients_K_sport.csv");
            loadCoefficients(calculatorId, "Kprof", "classpath:bootstrap/ACC/NS_CLASSIC/coefficients_Kprof.csv");
            loadCoefficients(calculatorId, "Ksport1", "classpath:bootstrap/ACC/NS_CLASSIC/coefficients_Ksport1.csv");
            loadCoefficients(calculatorId, "Ksrok", "classpath:bootstrap/ACC/NS_CLASSIC/coefficients_Ksrok.csv");

            calculatorId = loadCalculators("classpath:bootstrap/GAD/calculator__1_0.json");
            calculatorId = loadCalculators("classpath:bootstrap/GAD/calculator__1_1.json");
            calculatorId = loadCalculators("classpath:bootstrap/GAD/calculator__1_2.json");
            calculatorId = loadCalculators("classpath:bootstrap/GAD/calculator__1_3.json");
            calculatorId = loadCalculators("classpath:bootstrap/GAD/calculator__1_11.json");
            calculatorId = loadCalculators("classpath:bootstrap/GAD/calculator__1_12.json");
            // example explicit calls
            // loadCoefficients(calculatorIdMap.get("classpath:bootstrap/ACC/calculator_HC+sport.json"), "K_Age", calculatorIdMap);
            calculatorId = loadCalculators("classpath:bootstrap/CRG/calculator__1_0.json");

            saveTestExamples(TENANT_ID, "CARGO_GENERAL", 1L, "classpath:bootstrap/CRG/test-quote.json", "classpath:bootstrap/CRG/test-policy.json");
        });
    }

    private void saveTestExamples(Long tenantId, String productCode, Long versionNo, String quoteJsonFile, String policyJsonFile) {
        Long productId = productService.getProductByCode(TENANT_ID, productCode, true).getId();

        if (productId == null) {
            log.warn("Product not found: {}", productCode);
            return;
        }

        String quoteJson = readTextResource(quoteJsonFile);
        String policyJson = readTextResource(policyJsonFile);
        productTestService.saveTestQuote(tenantId, productId, versionNo, quoteJson);
        productTestService.saveTestPolicy(tenantId, productId, versionNo, policyJson);
        log.info(
                "Saved product test quote/policy: productId={} versionNo={} quoteFile={} policyFile={}",
                productId,
                versionNo,
                quoteJsonFile,
                policyJsonFile
        );
    }

    private void loadLobs(List<String> resources) {
        for (String location : resources) {
            try {
                LobModel model = readResource(location, LobModel.class);
                lobService.create(TENANT_ID, model);
            } catch (Exception e) {
                log.warn("LOB already exists in system or cannot be loaded: {}", location);
            }
        }
    }

    private void loadProducts(List<String> resources) {
        for (String location : resources) {
            try {
                ProductVersionModel model = readResource(location, ProductVersionModel.class);
                productService.create(TENANT_ID, model);
            } catch (Exception e) {
                log.warn("Product already exists in system or cannot be loaded: {}", location);
            }
        }
    }

    private Long loadCalculators(String location) {
        Long calculatorId = null;

       
            try {
                CalculatorModel model = readResource(location, CalculatorModel.class);
                if (model.getProductCode() == null || model.getVersionNo() == null || model.getPackageNo() == null) {
                    log.warn("Calculator JSON missing productCode/versionNo/packageNo: {}", location);
                    return null;
                }
                log.warn("Getting product by code: {} productCode={} versionNo={}", location, model.getProductCode(), model.getVersionNo());
                ProductVersionModel product = productService.getProductByCodeAndVersionNo(
                        TENANT_ID,
                        model.getProductCode(),
                        model.getVersionNo()
                );
                Long productId = product.getId();
                log.warn("Product found: {} productId={}", location, productId);
                if (productId == null) {
                    log.warn("Cannot resolve productId for calculator: {}", location);
                    return null;
                }
                log.info("Creating calculator: {} productId={} productCode={} versionNo={} packageNo={}", location, productId, model.getProductCode(), model.getVersionNo(), model.getPackageNo());
                CalculatorModel calculator = calculatorService.getCalculator(TENANT_ID, productId, model.getVersionNo(), model.getPackageNo());
                if (calculator != null) {
                    calculatorId = calculator.getId();
                    return calculatorId;
                }

                calculatorId = calculatorService.createCalculator(
                        TENANT_ID,
                        productId,
                        model.getVersionNo(),
                        model.getPackageNo(),
                        null
                ).getId();
                calculatorIdMap.put(location, calculatorId);
                log.info("Updating calculator: {} productId={} productCode={} versionNo={} packageNo={}", location, productId, model.getProductCode(), model.getVersionNo(), model.getPackageNo());
                calculatorService.updateCalculator(
                        TENANT_ID,
                        productId,
                        model.getProductCode(),
                        model.getVersionNo(),
                        model.getPackageNo(),
                        model
                );
            } catch (Exception e) {
                log.warn("Calculator already exists in system or cannot be loaded: {}", location);
                log.warn("Error: {}", e.getMessage());
                e.printStackTrace();
            }
        
        return calculatorId;
    }

    private void loadCoefficients(Long calculatorId, String coeffName, String filename) {
        if (coeffName == null || coeffName.isBlank()) {
            log.warn("Coefficient code is empty, skip load");
            return;
        }
        if (calculatorId == null) {
            log.warn("CalculatorId is not resolved for coefficient {}", coeffName);
            return;
        }

        String location = filename;
        log.warn("Loading coefficients: {} calculatorId={} coeffName={}", location, calculatorId, coeffName);

        try {
            List<CoefficientDataRow> rows = readCoefficientCsv(location);
            if (rows.isEmpty()) {
                log.warn("No coefficient rows in file: {}", location);
                return;
            }
            coefficientService.replaceTable(calculatorId, coeffName, rows);
            log.info("Coefficients loaded: {} code={} rows={}", location, coeffName, rows.size());
        } catch (Exception e) {
            log.warn("Coefficient already exists in system or cannot be loaded: {}", location);
            log.warn("Error: {}", e.getMessage());
        }
    }

    private List<CoefficientDataRow> readCoefficientCsv(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("Coefficient CSV not found: " + location);
            }
            try (InputStream is = resource.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<CoefficientDataRow> rows = new ArrayList<>();
                String line;
                int lineNo = 0;
                while ((line = reader.readLine()) != null) {
                    lineNo++;
                    if (lineNo == 1) {
                        continue; // headers
                    }
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) {
                        continue;
                    }
                    String[] cols = trimmed.split(",", -1);
                    if (cols.length < 2) {
                        continue;
                    }
                    CoefficientDataRow row = new CoefficientDataRow();
                    List<String> conditions = new ArrayList<>();
                    for (int i = 0; i < cols.length - 1; i++) {
                        conditions.add(cleanCsvCell(cols[i]));
                    }
                    row.setConditionValue(conditions);
                    row.setResultValue(parseResultValue(cleanCsvCell(cols[cols.length - 1])));
                    rows.add(row);
                }
                return rows;
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read coefficient CSV: " + location, e);
        }
    }

    private String cleanCsvCell(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    private BigDecimal parseResultValue(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim()
                .replace("\"", "")
                .replace(" ", "")
                .replace(",", ".");
        if (normalized.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException e) {
            log.warn("Invalid coefficient result value: '{}', using null", raw);
            return null;
        }
    }

    private <T> T readResource(String location, Class<T> type) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("Bootstrap resource not found: " + location);
            }
            try (InputStream is = resource.getInputStream()) {
                return objectMapper.readValue(is, type);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load bootstrap resource: " + location, e);
        }
    }

    private String readTextResource(String location) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("Bootstrap text resource not found: " + location);
            }
            try (InputStream is = resource.getInputStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load bootstrap text resource: " + location, e);
        }
    }

    private void runAsSystem(Runnable action) {
        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UserDetailsImpl systemUser = new UserDetailsImpl(
                ACCOUNT_ID,          // id
                "bootstrap-system",  // username
                "demo",              // tenantCode
                TENANT_ID,           // tenantId
                ACCOUNT_ID,          // accountId
                "bootstrap",         // accountName
                12L,                 // clientId
                "sys",               // clientName
                "TNT_ADMIN",         // userRole
                new HashSet<>(),     // productRoles
                true,                // isDefault
                10L,          // actingAccountId
                String.valueOf(ACCOUNT_ID) // accountPath
        );
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                systemUser,
                "N/A",
                systemUser.getAuthorities()
        ));
        SecurityContextHolder.setContext(context);
        try {
            action.run();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }
}


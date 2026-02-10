package ru.pt.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.commission.CommissionDto;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.db.PolicyStatus;
import ru.pt.api.dto.errors.ErrorConstants;
import ru.pt.api.dto.errors.ErrorModel;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.api.dto.exception.UnprocessableEntityException;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.payment.PaymentType;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.ValidatorType;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
//import ru.pt.api.dto.product.LobModel;
//import ru.pt.api.dto.product.LobVar;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.commission.CommissionService;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.numbers.NumberGeneratorService;
import ru.pt.api.service.process.PostProcessService;
import ru.pt.api.service.process.PreProcessService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.api.service.process.ValidatorService;
import ru.pt.api.service.product.LobService;
import ru.pt.api.service.product.ProductService;
import ru.pt.api.service.product.VersionManager;
import ru.pt.api.utils.JsonProjection;

import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.utils.JsonSetter;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.ClientService;

import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.TextDocumentView;
import ru.pt.domain.model.VariableContext;
import ru.pt.files.service.email.EmailGateService;
import ru.pt.payments.service.PaymentClientSwitch;
import ru.pt.process.utils.MdcWrapper;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;

import java.math.BigDecimal;

import ru.pt.api.dto.commission.CommissionAction;
import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.dto.process.ProcessList;


@Service
@RequiredArgsConstructor
public class ProcessOrchestratorService implements ProcessOrchestrator {

    private final Logger logger = LoggerFactory.getLogger(ProcessOrchestratorService.class);

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final StorageService storageService;
    private final SecurityContextHelper securityContextHelper;
    private final NumberGeneratorService numberGeneratorService;
    private final ProductService productService;
    private final VersionManager versionManager;
    private final CalculatorService calculatorService;
    private final LobService lobService;
    private final ValidatorService validatorService;
    private final PreProcessService preProcessService;
    private final PostProcessService postProcessService;
    private final PaymentClientSwitch paymentClient;
    private final ClientService clientService;
    private final EmailGateService emailGateService;
    private final TextDocumentView textDocumentView;
    private final AuthorizationService authorizationService;
    private final CommissionService commissionService;
    
    /**
     * Get current authenticated user from security context.
     * @return AuthenticatedUser representing the current user
     * @throws UnauthorizedException if user is not authenticated
     */
    protected AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> {
                    ErrorModel errorModel = ErrorConstants.createErrorModel(
                            401,
                            "Unable to get current user from security context",
                            ErrorConstants.DOMAIN_AUTH,
                            ErrorConstants.REASON_UNAUTHORIZED,
                            "securityContext"
                    );
                    return new UnauthorizedException(errorModel);
                });
    }

    private PolicyDTO policyFromJson(String policy) {
        logger.debug("Parsing policy from JSON");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
            

        PolicyDTO policyDTO = null;
        try {
            policyDTO = objectMapper.readValue(policy, PolicyDTO.class);
            logger.debug("Successfully parsed policy. productCode={}", policyDTO.getProductCode());
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse policy JSON: {}", e.getMessage(), e);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.invalidJsonFormat("policy"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_INVALID_FORMAT,
                "policy"
            );
            throw new BadRequestException(errorModel);
        }
        return policyDTO;
    }
    private String policyToJson(PolicyDTO policyDTO) {
        logger.debug("Serializing policy to JSON. policyNumber={}", policyDTO.getPolicyNumber());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.registerModule(new JavaTimeModule());
            
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                        
            String json = objectMapper.writeValueAsString(policyDTO);
            logger.debug("Successfully serialized policy to JSON");
            return json;
            
        } catch (JsonProcessingException e) {
            logger.error("Error serializing PolicyDTO to JSON: {}", e.getMessage(), e);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                500,
                "Failed to serialize policy to JSON: " + e.getMessage(),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_INTERNAL_ERROR,
                "policy"
            );
            throw new InternalServerErrorException(errorModel);
        } catch (Exception e) {
            logger.error("Unexpected error serializing PolicyDTO to JSON: {}", e.getMessage(), e);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                500,
                "Unexpected error serializing policy: " + e.getMessage(),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_INTERNAL_ERROR,
                "policy"
            );
            throw new InternalServerErrorException(errorModel);
        }
    }

    private void calculatePremium(PolicyDTO policyDTO, ProductVersionModel product, VariableContext varCtx) {
        logger.debug("Calculating premium. productCode={}, packageCode={}", 
            product.getCode(), policyDTO.getInsuredObjects().get(0).getPackageCode());

        // Print all variable definitions
/*
        System.out.println("=== Variable Definitions ===");
        varCtx.getDefinitions().forEach(def -> {
            System.out.println("varCode: " + def.getCode() + ", varDataType: " + def.getType());
        });
        System.out.println("===========================");
*/
        addMandatoryVars(policyDTO, varCtx.getDefinitions());

        CalculatorModel calculatorModel = calculatorService.getCalculator(
            product.getId(), 
            product.getVersionNo(), 
            policyDTO.getInsuredObjects().get(0).getPackageCode());

        if (calculatorModel != null) {
            logger.debug("Running calculator for product {} version {} package {}", 
                product.getId(), product.getVersionNo(), policyDTO.getInsuredObjects().get(0).getPackageCode());
            calculatorService.runCalculator(
                product.getId(), 
                product.getVersionNo(), 
                policyDTO.getInsuredObjects().get(0).getPackageCode(), 
                varCtx );

                postProcessService.setCovers(policyDTO, varCtx);

        } else {
            logger.warn("No calculator found for product {} version {} package {}", 
                product.getId(), product.getVersionNo(), policyDTO.getInsuredObjects().get(0).getPackageCode());
        }

        
        policyDTO.getProcessList().setVars(varCtx.getValues());

        // Если расчет велся по договору без покрытий то можно записать премию в pl_premium
        BigDecimal policyPremium = varCtx.getDecimal("pl_premium");
        if (policyPremium != null && policyPremium.compareTo(BigDecimal.ZERO) > 0) {
            policyDTO.setPremium(policyPremium);    
            logger.debug("Premium calculated. policyPremium={}", policyPremium);
        } else {
            BigDecimal totalPremium = calculateTotalPremium(policyDTO);
            policyDTO.setPremium(totalPremium);
            logger.debug("Premium calculated. totalPremium={}", totalPremium);
        }

//        for ( PvVarDefinition var : varCtx.getDefinitions()) {
//            logger.info("Var {} {}", var.getCode(),  varCtx.getString(var.getCode()));
//        }

    }

    private void addMandatoryVars(PolicyDTO policyDTO, List<PvVarDefinition> varDefinitions) {
        //varDefinitions.add(new PvVarDefinition("pl_product", "productCode", PvVarDefinition.Type.STRING, "IN"));
        //varDefinitions.add(new PvVarDefinition("pl_package", "packageCode", PvVarDefinition.Type.STRING, "IN"));
        for (InsuredObject insuredObject : policyDTO.getInsuredObjects()) {
            for (Cover cover : insuredObject.getCovers()) {
                String coverCode = cover.getCover().getCode();
/* 
                varDefinitions.add(new PvVarDefinition("co_" + coverCode + "_sumInsured", "", PvVarDefinition.Type.NUMBER, "VAR"));
                varDefinitions.add(new PvVarDefinition("co_" + coverCode + "_premium", "", PvVarDefinition.Type.NUMBER, "VAR"));
                varDefinitions.add(new PvVarDefinition("co_" + coverCode + "_deductibleNr", "", PvVarDefinition.Type.NUMBER, "VAR"));
                varDefinitions.add(new PvVarDefinition("co_" + coverCode + "_limitMin", "", PvVarDefinition.Type.NUMBER, "VAR"));
                varDefinitions.add(new PvVarDefinition("co_" + coverCode + "_limitMax", "", PvVarDefinition.Type.NUMBER, "VAR"));
*/
                varDefinitions.add(PvVarDefinition.fromPvVar(PvVar.varSumInsured(coverCode)));
                varDefinitions.add(PvVarDefinition.fromPvVar(PvVar.varPremium(coverCode)));
                varDefinitions.add(PvVarDefinition.fromPvVar(PvVar.varDeductibleNr(coverCode)));
                varDefinitions.add(PvVarDefinition.fromPvVar(PvVar.varLimitMin(coverCode)));
                varDefinitions.add(PvVarDefinition.fromPvVar(PvVar.varLimitMax(coverCode)));
            }
        }
    }

    @Override
    public String calculate(String policy) {
        logger.info("Starting calculate process");

        AuthenticatedUser user = getCurrentUser();
        // Считать и продавать могут только учетки с ролью ACCOUNT & SUB
        // Для них расчет идет по продовой версии. Все остальные должны иметь разрешение TESTER, тогда они могут считаться по версии продукта DEV
        boolean iAmTester = authorizationService.userHasPermition(user, AuthZ.ResourceType.POLICY, AuthZ.Action.TEST);



        // 1. JSON → DTO contract
        PolicyDTO policyDTO;
        try {
            policyDTO = policyFromJson(policy);
        } catch (BadRequestException e) {
            // Re-throw BadRequestException as-is
            throw e;
        } catch (Exception e) {
            logger.error("Invalid policy JSON in calculate: {}", e.getMessage(), e);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.invalidJsonFormat("calculate request body"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_INVALID_FORMAT,
                "requestBody"
            );
            throw new BadRequestException(errorModel);
        }

        policyDTO.setProcessList(new ProcessList(ProcessList.QUOTE));
        /* Удалить возможный хлам */
        policyDTO.setPremium(null);
        policyDTO.setPolicyNumber(null);
        policyDTO.setProductVersion(0);
        policyDTO.setId(null);
        policyDTO.setStatusCode("NEW");

        if (iAmTester) {
            policyDTO.getProcessList().setProductVersionStatus(ProcessList.DEV);
        } else {
            policyDTO.getProcessList().setProductVersionStatus(ProcessList.PROD);
        }
        // 3. Получить продукт
        ProductVersionModel product;
        String productCode = policyDTO.getProductCode();
        logger.debug("Fetching product. productCode={}", productCode);

        

        try {
            // Для тестера ghjlern d cnfnect дев
            product = productService.getProductByCode(productCode, iAmTester);
            logger.debug("Product fetched. productId={}, versionNo={}", product.getId(), product.getVersionNo());
        } catch (NotFoundException e) {
            // Re-throw NotFoundException as-is
            throw e;
        } catch (Exception e) {
            logger.warn("Invalid product code: {}", productCode);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                422,
                ErrorConstants.productNotFound(productCode),
                ErrorConstants.DOMAIN_PRODUCT,
                ErrorConstants.REASON_NOT_FOUND,
                "productCode"
            );
            throw new UnprocessableEntityException(errorModel);
        }

        authorizationService.check(user, AuthZ.ResourceType.PRODUCT, product.getId().toString(), null, AuthZ.Action.QUOTE);
        
        // 4. Применение метаданных продукта
        try {
            preProcessService.applyProductMetadata(policyDTO, product);
        } catch (Exception e) {
            logger.error("Error applying product metadata for productCode={}: {}", productCode, e.getMessage(), e);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                422,
                String.format("Error applying product metadata for productCode '%s': %s", productCode, e.getMessage()),
                ErrorConstants.DOMAIN_PRODUCT,
                ErrorConstants.REASON_INTERNAL_ERROR,
                "productCode"
            );
            throw new UnprocessableEntityException(errorModel);
        }


        // 4.5 Проверить желаемую комиссию агента
        CommissionDto commissionDTO = new CommissionDto();
        if (policyDTO.getCommission()!=null) { commissionDTO = policyDTO.getCommission(); };
        // задана %
        if ( commissionDTO != null && commissionDTO.getRequestedCommissionRate() != null ) {
            commissionService.checkRequestedCommissionRate(
                commissionDTO.getRequestedCommissionRate(), 
                user.getAccountId(), 
                product.getId(), 
                CommissionAction.SALE);   // ToDo make enum
        // Если норм то меняет % на запрашиваемый
            policyDTO.getCommission().setAppliedCommissionRate(policyDTO.getCommission().getRequestedCommissionRate() );
        }
        
        // 5. DTO → JSON (эталонная модель)
        String policyJSON = policyToJson(policyDTO);
        
        // 6. PvVar → PvVarDefinition
        List<PvVarDefinition> varDefinitions = 
            product.getVars().stream()
                .map(this::toDefinition)
                .toList();


        // 7. Runtime-контекст
        VariableContext varCtx = new VariableContext(policyJSON, varDefinitions);

        // 8. Предобработка (если нужно задать значения явно)
        preProcessService.enrichVariables(varCtx);

        // 9. Валидация (lazy!)
        logger.debug("Validating policy for QUOTE");
        List<ValidationError> errors = validatorService.validate(ValidatorType.QUOTE, product, varCtx);

        if (!errors.isEmpty()) {
            logger.warn("Validation failed for QUOTE. errors={}", errors.size());
            List<ErrorModel.ErrorDetail> errorDetails = errors.stream()
                    .map(err -> new ErrorModel.ErrorDetail(
                        ErrorConstants.DOMAIN_VALIDATION,
                        ErrorConstants.REASON_VALIDATION_FAILED,
                        err.getReason(),
                        err.getPath()
                    ))
                    .collect(Collectors.toList());
            String errorMessage = "Validation failed: " + errors.stream()
                    .map(ValidationError::getReason)
                    .collect(Collectors.joining(", "));
            ErrorModel errorModel = new ErrorModel(400, errorMessage, errorDetails);
            throw new BadRequestException(errorModel);
        }

        // 10. Расчёт премии (lazy!)
        calculatePremium(policyDTO, product, varCtx);

        // 11. Перенос результатов в DTO
        //policyDTO.setPremium(ctx.getDecimal("PREMIUM"));
        String ph_digest = textDocumentView.get(varCtx, "ph_digest");
        String io_digest = textDocumentView.get(varCtx, "io_digest");
        policyDTO.getProcessList().setPhDigest(ph_digest);
        policyDTO.getProcessList().setIoDigest(io_digest);

        // 11. Рассчет КВ
        commissionDTO = commissionService.calculateCommission(
            commissionDTO.getRequestedCommissionRate(), 
            user.getAccountId(), 
            product.getId(), 
            CommissionAction.SALE,
            policyDTO.getPremium()
        );

        policyDTO.setCommission(commissionDTO);
        //    requestedCommissionRate, accountId, productId, policy, premium)

        // 12. Ответ
        logger.info("Calculate process completed. premium={}", policyDTO.getPremium());

        if (policyDTO.getPremium() == null || policyDTO.getPremium().compareTo(BigDecimal.ZERO) <= 0) {
            throw new UnprocessableEntityException(new ErrorModel(0, "Запрос не соответствует условиям тарифа.", "Calculator", "premium=0", "premium")); 
        }
        return policyToJson(policyDTO);

    }

    @Override
    public String save(String policy) {
        logger.info("Starting save process");
        AuthenticatedUser user = getCurrentUser();

        // Считать и продавать могут только учетки с ролью ACCOUNT & SUB
        // Для них расчет идет по продовой версии. Все остальные должны иметь разрешение TESTER, тогда они могут считаться по версии продукта DEV
        boolean iAmTester = authorizationService.userHasPermition(user, AuthZ.ResourceType.POLICY, AuthZ.Action.TEST);
        
        logger.info("current accoutn - id:{}, name:{}, role:{}", user.getAccountId(), user.getAccountName(), user.getUserRole());
        // 1. JSON → DTO core
        PolicyDTO policyDTO = policyFromJson(policy);
        // Добавить помойку
        policyDTO.setProcessList(new ProcessList(ProcessList.SAVE));
        /* Удалить возможный хлам */
        policyDTO.setPremium(null);
        policyDTO.setPolicyNumber(null);
        policyDTO.setProductVersion(0);
        policyDTO.setId(null);
        policyDTO.setStatusCode("NEW");
        
        if (iAmTester) {
            policyDTO.getProcessList().setProductVersionStatus(ProcessList.DEV);
        } else {
            policyDTO.getProcessList().setProductVersionStatus(ProcessList.PROD);
        }

        // 3. Продукт
        String productCode = policyDTO.getProductCode();
        logger.debug("Fetching product for save. productCode={}", productCode);
        var product = productService.getProductByCode(productCode, iAmTester);
        logger.debug("Product fetched for save. productId={}, versionNo={}", product.getId(), product.getVersionNo());

        authorizationService.check(user, AuthZ.ResourceType.PRODUCT, null, null, AuthZ.Action.SELL);

        // 4. Применение метаданных продукта
        preProcessService.applyProductMetadata(policyDTO, product);

        // 4.5 Проверить желаемую комиссию агента
        CommissionDto commissionDTO = new CommissionDto();
        if (policyDTO.getCommission()!=null) { commissionDTO = policyDTO.getCommission(); };
        // задана %
        if ( commissionDTO != null && commissionDTO.getRequestedCommissionRate() != null ) {
            commissionService.checkRequestedCommissionRate(
                commissionDTO.getRequestedCommissionRate(), 
                user.getAccountId(), 
                product.getId(), 
                CommissionAction.SALE);
        // Если норм то меняет % на запрашиваемый
            policyDTO.getCommission().setAppliedCommissionRate(policyDTO.getCommission().getRequestedCommissionRate() );
        }


        // 5. DTO → JSON (эталонная модель)
        /* обогащенные данные */
        String policyJSON = policyToJson(policyDTO);

        // 6. PvVar → PvVarDefinition
        List<PvVarDefinition> varDefinitions = 
            product.getVars().stream()
                .map(this::toDefinition)
                .toList();

        // 7. Runtime-контекст
        VariableContext varCtx = new VariableContext(policyJSON, varDefinitions);

        // 8. Предобработка (если нужно задать значения явно)
        preProcessService.enrichVariables(varCtx);

        // 9. Валидация (lazy!)
        logger.debug("Validating policy for QUOTE and SAVE");
        List<ValidationError> errors = validatorService.validate(ValidatorType.QUOTE, product, varCtx);
        errors.addAll(validatorService.validate(ValidatorType.SAVE, product, varCtx));

        if (!errors.isEmpty()) {
            logger.warn("Validation failed for SAVE. errors={}", errors.size());
            // TODO нормальную структуру под ошибки
            throw new BadRequestException(errors.stream()
                    .map(ValidationError::getReason)
                    .collect(Collectors.joining(","))
            );
        }

        // 10. Расчёт премии (lazy!)
        calculatePremium(policyDTO, product, varCtx);
  
        // 11. Рассчет КВ
        commissionDTO = commissionService.calculateCommission(
            commissionDTO.getRequestedCommissionRate(), 
            user.getAccountId(), 
            product.getId(), 
            CommissionAction.SALE,
            policyDTO.getPremium()
        );

        policyDTO.setCommission(commissionDTO);
        
        // 11. Получить номер полиса
        String nextNumber = numberGeneratorService.getNextNumber(varCtx, productCode);
        policyDTO.setPolicyNumber(nextNumber);
        logger.debug("Generated policy number: {}", nextNumber);

        // Доп атрибуты вычислить
        //String ph_digest = VariablesService.getPhDigest(product.getPhType(), varCtx);
        //String io_digest = VariablesService.getIoDigest(product.getIoType(), varCtx);

        String ph_digest = textDocumentView.get(varCtx, "ph_digest");
        String io_digest = textDocumentView.get(varCtx, "io_digest");

        logger.debug("Generated digests. phDigest={}, ioDigest={}", ph_digest, io_digest);

        policyDTO.getProcessList().setPhDigest(ph_digest);
        policyDTO.getProcessList().setIoDigest(io_digest);


        // 12. Сохранить договор в хранилище
        logger.debug("Saving policy to storage. policyNumber={}", nextNumber);
        storageService.save(policyDTO, getCurrentUser());

        // 13. Ответ, удалить помойку
        policyDTO.setProcessList(null);
        String newPolicyJson = policyToJson(policyDTO);

        // TODO async send KID + draft print form

        logger.info("Save process completed. policyNumber={}, premium={}", nextNumber, policyDTO.getPremium());
        return newPolicyJson;
    }

    @Override
    public String update(String policy) {
        return "";
    }

    @Override
    public String createAddendum(String policy) {
        return "";
    }

    @Override
    public PaymentData payment(PaymentData paymentData) {
        try {
            logger.info("Received payment request {}", objectWriter.writeValueAsString(paymentData));
        } catch (Exception e) {
            logger.error("Can't write json message - {}", e.getMessage(), e);
        }
        PaymentData response = paymentClient.getCurrentPaymentClient().createPayment(paymentData);
        if (paymentData.getPaymentType().equals(PaymentType.CASH)) {
            logger.info("Received CASH request, no need to wait for payment confirmation");
            executorService.submit(() -> paymentCallback(paymentData.getPolicyNumber()));
        }
        return response;
    }

    @Override
    public void paymentCallback(String policyId) {
        // Validate policyId
        if (policyId == null || policyId.trim().isEmpty()) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                ErrorConstants.missingRequiredField("policyId"),
                ErrorConstants.DOMAIN_PAYMENT,
                ErrorConstants.REASON_MISSING_REQUIRED,
                "policyId"
            );
            throw new BadRequestException(errorModel);
        }
        
        UUID policyUuid;
        try {
            policyUuid = UUID.fromString(policyId);
        } catch (IllegalArgumentException e) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                400,
                String.format("Invalid UUID format for policyId: %s", policyId),
                ErrorConstants.DOMAIN_PAYMENT,
                ErrorConstants.REASON_INVALID_FORMAT,
                "policyId"
            );
            throw new BadRequestException(errorModel);
        }
        
        // set policy status
        PolicyData policyData = storageService.getPolicyById(policyUuid);
        if (policyData == null) {
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                404,
                ErrorConstants.policyNotFoundById(policyId),
                ErrorConstants.DOMAIN_PAYMENT,
                ErrorConstants.REASON_NOT_FOUND,
                "policyId"
            );
            throw new NotFoundException(errorModel);
        }
        JsonSetter jsonSetter = new JsonSetter(policyData.getPolicy());
        jsonSetter.setRawValue("status", "PAID");
        policyData.setPolicy(jsonSetter.writeValue());
        policyData.setPolicyStatus(PolicyStatus.PAID);
        storageService.update(policyData);

        Client clientById = clientService.getClientById(policyData.getPolicyIndex().getClientAccountId());
        ClientConfiguration clientConfiguration = clientById.getClientConfiguration();
        if (clientConfiguration.isSendEmailAfterBuy()) {
            emailGateService.resolveForCurrentUser(clientById.getId())
                    .sendEmail(emailGateService.buildEmailMessage(policyData), clientConfiguration);
        } else {
            logger.info("For client sending email is disabled, skipping");
        }
        if (clientConfiguration.isSendSmsAfterBuy()) {
            // TODO send sms
        } else {
            logger.info("For client sending sms is disable, skipping");
        }
    }

    /* 
    @Override
    public PolicyData createPolicy(String policy) {
        logger.info("Creating new policy");
        var jsonProjection = new JsonProjection(policy);

        var productCode = jsonProjection.getProductCode();
        logger.debug("Creating policy for productCode={}", productCode);

        var version = versionManager.getLatestVersionByProductCode(productCode);

        var uuid = UUID.randomUUID();
        MdcWrapper.putId(uuid.toString());
        logger.debug("Generated policy UUID: {}", uuid);

        PolicyData policyData = storageService.save(policy, getCurrentUser(), version, uuid);
        logger.info("Policy created. policyId={}", uuid);
        return policyData;
    }
        */

    @Override
    public PolicyData updatePolicy(String policyNumber, String policy) {
        logger.info("Updating policy. policyNumber={}", policyNumber);

        var userData = getCurrentUser();
        var policyData = storageService.getPolicyByNumber(policyNumber);
        if (policyData == null) {
            logger.warn("Policy not found for update. policyNumber={}", policyNumber);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                404,
                ErrorConstants.policyNotFound(policyNumber),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_NOT_FOUND,
                "policyNumber"
            );
            throw new NotFoundException(errorModel);
        }
        
        if (policyData.getPolicyStatus() != PolicyStatus.NEW) {
            logger.warn("Attempt to update policy with bad status. policyNumber={}, status={}", policyNumber, policyData.getPolicyStatus());
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                422,
                String.format("Cannot update policy with status '%s'. Only policies with status 'NEW' can be updated", policyData.getPolicyStatus()),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_INVALID,
                "policyStatus"
            );
            throw new UnprocessableEntityException(errorModel);
        }

        var policyIndex = policyData.getPolicyIndex();

        if (!policyIndex.getUserAccountId().equals(userData.getAccountId()) ||
                !policyIndex.getClientAccountId().equals(userData.getClientId())) {
            logger.warn("Unauthorized attempt to update policy. policyNumber={}", policyNumber);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                403,
                ErrorConstants.forbiddenAccess("policy", "User does not have access to this policy"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_FORBIDDEN,
                "policyNumber"
            );
            throw new ForbiddenException(errorModel);
        }

        var jsonProjection = new JsonProjection(policy);

        var productCode = jsonProjection.getProductCode();
        logger.debug("Updating policy with productCode={}", productCode);

        var version = versionManager.getLatestVersionByProductCode(productCode);

        PolicyData updatedPolicy = storageService.update(policy, userData, version, policyNumber);
        logger.info("Policy updated. policyNumber={}", policyNumber);
        return updatedPolicy;
    }

    @Override
    public PolicyData getPolicyById(UUID id) {
        logger.debug("Getting policy by ID. policyId={}", id);
        var userData = getCurrentUser();
        var policyData = storageService.getPolicyById(id);
        if (policyData == null) {
            logger.warn("Policy not found by ID. policyId={}", id);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                404,
                ErrorConstants.policyNotFoundById(id.toString()),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_NOT_FOUND,
                "policyId"
            );
            throw new NotFoundException(errorModel);
        }

        var policyIndex = policyData.getPolicyIndex();

        if (!policyIndex.getUserAccountId().equals(userData.getAccountId()) ||
                !policyIndex.getClientAccountId().equals(userData.getClientId())) {
            logger.warn("Unauthorized attempt to access policy. policyId={}", id);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                403,
                ErrorConstants.forbiddenAccess("policy", "User does not have access to this policy"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_FORBIDDEN,
                "policyId"
            );
            throw new ForbiddenException(errorModel);
        }

        logger.debug("Policy retrieved. policyId={}, policyNumber={}", id, policyIndex.getPolicyNumber());
        return policyData;
    }

    @Override
    public PolicyData getPolicyByNumber(String policyNumber) {
        logger.debug("Getting policy by number. policyNumber={}", policyNumber);
        var userData = getCurrentUser();
        var policyData = storageService.getPolicyByNumber(policyNumber);
        if (policyData == null) {
            logger.warn("Policy not found by number. policyNumber={}", policyNumber);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                404,
                ErrorConstants.policyNotFound(policyNumber),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_NOT_FOUND,
                "policyNumber"
            );
            throw new NotFoundException(errorModel);
        }

        var policyIndex = policyData.getPolicyIndex();

        if (!policyIndex.getUserAccountId().equals(userData.getAccountId()) ||
                !policyIndex.getClientAccountId().equals(userData.getClientId())) {
            logger.warn("Unauthorized attempt to access policy. policyNumber={}", policyNumber);
            ErrorModel errorModel = ErrorConstants.createErrorModel(
                403,
                ErrorConstants.forbiddenAccess("policy", "User does not have access to this policy"),
                ErrorConstants.DOMAIN_POLICY,
                ErrorConstants.REASON_FORBIDDEN,
                "policyNumber"
            );
            throw new ForbiddenException(errorModel);
        }

        logger.debug("Policy retrieved. policyNumber={}", policyNumber);
        return policyData;
    }

    private BigDecimal calculateTotalPremium(PolicyDTO policyDTO) {
        BigDecimal premium = BigDecimal.ZERO;
        InsuredObject insObject = policyDTO.getInsuredObjects().get(0);

        for (Cover cover : insObject.getCovers()) {
            if (cover.getPremium() != null) {
                premium = premium.add(cover.getPremium());
            }
        }
        return premium;
    }

    private PvVarDefinition toDefinition(PvVar var) {
        return PvVarDefinition.fromPvVar(var); 
    }
}

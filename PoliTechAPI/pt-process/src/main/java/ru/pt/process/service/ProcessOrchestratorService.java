package ru.pt.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.policy.Cover;
import ru.pt.api.dto.policy.Installment;
import ru.pt.api.dto.policy.InsuredObject;
import ru.pt.api.dto.policy.Commission;
import ru.pt.api.dto.policy.StdPolicy;
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
import ru.pt.api.dto.payment.CreateInstallmentsRequest;
import ru.pt.api.dto.payment.InstallmentDto;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.payment.PaymentType;
import ru.pt.api.dto.rules.RuleType;
import ru.pt.api.dto.rules.RuleValidationContext;
import ru.pt.api.service.rules.RuleValidationService;
import ru.pt.api.service.addon.PolicyAddOnService;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
//import ru.pt.api.dto.product.LobModel;
//import ru.pt.api.dto.product.LobVar;
import ru.pt.api.service.calculator.CalculatorService;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.numbers.NumberGeneratorService;
import ru.pt.api.service.payment.PaymentService;
import ru.pt.process.service.PostProcessService;
import ru.pt.api.service.process.ProcessOrchestrator;
import ru.pt.api.service.process.ValidatorService;

import ru.pt.api.utils.JsonProjection;
import ru.pt.api.utils.JsonSetter;

import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.context.RequestContext;
import ru.pt.auth.service.ClientService;
import ru.pt.domain.model.CalculatorContext;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.TextDocumentView;
import ru.pt.domain.model.VariableContext;
import ru.pt.domain.process.document.ProcessList;
import ru.pt.domain.process.document.ValidatorType;
import ru.pt.files.service.email.EmailGateService;
import ru.pt.payments.service.PaymentClientSwitch;
import ru.pt.process.utils.MdcWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ru.pt.api.dto.product.InsuranceCompanyDto;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;

import java.math.BigDecimal;

import ru.pt.api.dto.addon.PolicyAddOnDto;

@Service
@RequiredArgsConstructor
public class ProcessOrchestratorService implements ProcessOrchestrator {

    private final Logger logger = LoggerFactory.getLogger(ProcessOrchestratorService.class);

    private final ObjectWriter objectWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final StorageService storageService;
    private final SecurityContextHelper securityContextHelper;
    private final NumberGeneratorService numberGeneratorService;
    private final CalculatorService calculatorService;
    private final ValidatorService validatorService;
    private final PostProcessService postProcessService;
    private final PaymentClientSwitch paymentClient;
    private final ClientService clientService;
    private final EmailGateService emailGateService;
    private final AuthorizationService authorizationService;
    private final PaymentService paymentService;
    private final PolicyAddOnService policyAddOnService;
    private final RuleValidationService ruleValidationService;
    private final RequestContext requestContext;
    private final PolicyProcessSupport policyProcessSupport;
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

    private void calculatePremium(Long tenantId, StdPolicy stdPolicy, ProductVersionModel product, CalculatorContext varCtx) {
        logger.debug("Calculating premium. productCode={}, packageCode={}", 
            product.getCode(), stdPolicy.getInsuredObjects().get(0).getPackageCode());
        
        //Map<String, Object> allVars = stdPolicy.getMap();

        addMandatoryVars(stdPolicy, varCtx);

        CalculatorModel calculatorModel = calculatorService.getCalculator(
            tenantId,
            product.getId(), 
            product.getVersionNo(), 
            stdPolicy.getInsuredObjects().get(0).getPackageCode());

        if (calculatorModel != null) {
            logger.debug("Running calculator for product {} version {} package {}", 
                product.getId(), product.getVersionNo(), stdPolicy.getInsuredObjects().get(0).getPackageCode());
            calculatorService.runCalculator(
                tenantId,
                product.getId(), 
                product.getVersionNo(), 
                stdPolicy.getInsuredObjects().get(0).getPackageCode(), 
                varCtx );

                postProcessService.setCovers(stdPolicy.getInsuredObjects().get(0), varCtx);

        } else {
            logger.warn("No calculator found for product {} version {} package {}", 
                product.getId(), product.getVersionNo(), stdPolicy.getInsuredObjects().get(0).getPackageCode());
        }

        
        //stdPolicy.getProcessList().setVars(varCtx.getValues());

        // Если расчет велся по договору без покрытий то можно записать премию в pl_premium
        BigDecimal policyPremium = varCtx.getDecimal("pl_premium");
        if (policyPremium != null && policyPremium.compareTo(BigDecimal.ZERO) > 0) {
            stdPolicy.setPremium(policyPremium);    
            logger.debug("Premium calculated. policyPremium={}", policyPremium);
        } else {
            BigDecimal totalPremium = calculateTotalPremium(stdPolicy);
            stdPolicy.setPremium(totalPremium);
            logger.debug("Premium calculated. totalPremium={}", totalPremium);
        }

        BigDecimal sumInsured = varCtx.getDecimal("io_sumInsured");
        if (sumInsured != null ) {
            if ( sumInsured.compareTo(BigDecimal.ZERO) > 0) {
                stdPolicy.getInsuredObjects().get(0).setSumInsured(sumInsured);
                logger.debug("Sum insured calculated. sumInsured={}", sumInsured);
            } else {
                throw new UnprocessableEntityException(new ErrorModel(0, "Сумма страхования не соответсвует условиям тарифа.", "Calculator", "sumInsured=0", "sumInsured")); 
            }
        }
        varCtx.put("io_sumInsured", stdPolicy.getInsuredObjects().get(0).getSumInsured());
    }

    private void addMandatoryVars(StdPolicy stdPolicy, CalculatorContext varCtx) {
        //varDefinitions.add(new PvVarDefinition("pl_product", "productCode", PvVarDefinition.Type.STRING, "IN"));
        //varDefinitions.add(new PvVarDefinition("pl_package", "packageCode", PvVarDefinition.Type.STRING, "IN"));

        for (InsuredObject insuredObject : stdPolicy.getInsuredObjects()) {
            for (Cover cover : insuredObject.getCovers()) {
                String coverCode = cover.getCover().getCode();

                PvVarDefinition sumInsuredDef = PvVarDefinition.fromPvVar(PvVar.varSumInsured(coverCode));
                PvVarDefinition premiumDef = PvVarDefinition.fromPvVar(PvVar.varPremium(coverCode));
                PvVarDefinition deductibleNrDef = PvVarDefinition.fromPvVar(PvVar.varDeductibleNr(coverCode));

                varCtx.putDefinition(sumInsuredDef);
                varCtx.putDefinition(premiumDef);
                varCtx.putDefinition(deductibleNrDef);
// ##### TODO
                varCtx.put(sumInsuredDef.getCode(), null);
                varCtx.put(premiumDef.getCode(), null);
                varCtx.put(deductibleNrDef.getCode(), null);
            }
        }
    }

    @Override
    public StdPolicy quote(StdPolicy stdPolicy) {
        logger.info("Starting quote process");

        AuthenticatedUser user = getCurrentUser();
        String dataScope = policyProcessSupport.requireDataScope(user);
        policyProcessSupport.normalizeForProcess(stdPolicy, new ProcessList(ProcessList.QUOTE), dataScope);

        ProductVersionModel product = policyProcessSupport.loadProduct( user.getTenantId(), stdPolicy.getProductCode(), dataScope);
        authorizationService.check(user, AuthZ.ResourceType.PRODUCT, product.getId().toString(), null, AuthZ.Action.QUOTE);

        policyProcessSupport.applyProductMetadata(user.getTenantId(), stdPolicy, product);

        /* Проверка комиссии */
        Commission commission = policyProcessSupport.resolveCommission(stdPolicy);
        policyProcessSupport.validateRequestedCommission(stdPolicy, commission, user, product);

        /* Заполнить контекст */
        CalculatorContext varCtx = policyProcessSupport.initVarContext(stdPolicy, product);

        logger.debug("Validating policy for pre QUOTE");
        List<ValidationError> errors = new ArrayList<>();
        errors.addAll(validatorService.validate(ValidatorType.QUOTE, product, varCtx));
        errors.addAll(runCelValidation(RuleType.PRE_QUOTE_VALIDATION, user, product, varCtx));
        if (!errors.isEmpty()) { throwValidationErrors("QUOTE", errors); }

        /* Рассчитать премию */
        calculatePremium(user.getTenantId(), stdPolicy, product, varCtx);

        /* Выполнить пост-валидацию */
        List<ValidationError> postQuoteErrors = runCelValidation(RuleType.POST_QUOTE_VALIDATION, user, product, varCtx);
        if (!postQuoteErrors.isEmpty()) { throwValidationErrors("POST_QUOTE_VALIDATION", postQuoteErrors); }

        /* Применить digest'ы */
        policyProcessSupport.applyDigests(stdPolicy, varCtx);

        /* Рассчитать комиссию */
        commission = policyProcessSupport.calculateCommission(
                commission, user, product, stdPolicy.getPremium());
        stdPolicy.setCommission(commission);

        /* Проверить, что премии не отрицательная */
        logger.info("Quote process completed. premium={}", stdPolicy.getPremium());
        policyProcessSupport.assertPositivePremium(stdPolicy);
        policyProcessSupport.stripProcessListForProdResponse(stdPolicy);

        /* Добавить кроссы */
        List<PolicyAddOnDto> policyAddOns = policyAddOnService.checkRequestedAddOns(product, varCtx, stdPolicy.getOptions());
        stdPolicy.setOptions(policyAddOns);

        return stdPolicy;
    }

    @Override
    public StdPolicy save(StdPolicy stdPolicy) {
        logger.info("Starting save process");

        AuthenticatedUser user = getCurrentUser();
        String dataScope = policyProcessSupport.requireDataScope(user);
        policyProcessSupport.normalizeForProcess(stdPolicy, new ProcessList(ProcessList.SAVE), dataScope);

        ProductVersionModel product = policyProcessSupport.loadProduct(user.getTenantId(), stdPolicy.getProductCode(), dataScope);

        if (dataScope.equals("PROD")) {
            authorizationService.check(user, AuthZ.ResourceType.POLICY, null, null, AuthZ.Action.SELL);
            authorizationService.checkProductAction(user, Long.valueOf(product.getId()), AuthZ.Action.SELL);
        }

        policyProcessSupport.applyProductMetadata(user.getTenantId(), stdPolicy, product);

        /* Проверка комиссии */
        Commission commission = policyProcessSupport.resolveCommission(stdPolicy);
        policyProcessSupport.validateRequestedCommission(stdPolicy, commission, user, product);

        /* Заполнить контекст */
        CalculatorContext varCtx = policyProcessSupport.initVarContext(stdPolicy, product);

        logger.debug("Validating policy for QUOTE and SAVE");
        List<ValidationError> errors = new ArrayList<>();
        errors.addAll(validatorService.validate(ValidatorType.QUOTE, product, varCtx));
        errors.addAll(validatorService.validate(ValidatorType.SAVE, product, varCtx));
        errors.addAll(runCelValidation(RuleType.PRE_QUOTE_VALIDATION, user, product, varCtx));
        errors.addAll(runCelValidation(RuleType.PRE_SAVE_VALIDATION, user, product, varCtx));
        if (!errors.isEmpty()) { throwValidationErrors("SAVE", errors);}

        calculatePremium(user.getTenantId(), stdPolicy, product, varCtx);

        /* Выполнить пост-валидацию */
        List<ValidationError> postQuoteErrors = runCelValidation(RuleType.POST_QUOTE_VALIDATION, user, product, varCtx);
        if (!postQuoteErrors.isEmpty()) { throwValidationErrors("POST_QUOTE_VALIDATION", postQuoteErrors); }

        commission = policyProcessSupport.calculateCommission( commission, user, product, stdPolicy.getPremium());
        stdPolicy.setCommission(commission);

        String nextNumber = numberGeneratorService.getNextNumber(user.getTenantId(), product.getNumberGeneratorDescription(), varCtx);
        stdPolicy.setPolicyNumber(nextNumber);
        logger.debug("Generated policy number: {}", nextNumber);

        /* Применить digest'ы */
        policyProcessSupport.applyDigests(stdPolicy, varCtx);

        /* Создать график платежей */
        CreateInstallmentsRequest installmentsRequest = new CreateInstallmentsRequest();
        installmentsRequest.setAmount(stdPolicy.getPremium());
        installmentsRequest.setCurrency("RUB");
        if (stdPolicy.getStartDate() != null) {
            installmentsRequest.setStartDate(stdPolicy.getStartDate().toLocalDate());
        }
        String installmentType = stdPolicy.getInstallmentType();
        if (installmentType == null || installmentType.isBlank()) {
            installmentType = "SINGLE";
        }
        installmentsRequest.setInstallmentType(installmentType.trim());

        List<InstallmentDto> installmentsDto = paymentService.createInstallments(user.getTenantId(), installmentsRequest);
        List<Installment> installments = new ArrayList<>();
        for (InstallmentDto installmentDto : installmentsDto) {
            installments.add(new Installment(
                    installmentDto.getInstallmentNr(),
                    installmentDto.getDueDate(),
                    installmentDto.getAmount()));
        }
        stdPolicy.setInstallments(installments);

        /* Проверить, что договор не прошел подтверждение */
        List<ValidationError> postSaveErrors = runCelValidation(RuleType.UNDERWRITING, user, product, varCtx);
        if (!postSaveErrors.isEmpty()) {
            stdPolicy.setStatusCode(PolicyStatus.UNDERWRITING.name());
        } else {
            stdPolicy.setStatusCode(PolicyStatus.ISSUED.name());
        }

        /* Сохранить договор в хранилище */
        logger.debug("Saving policy to storage. policyNumber={}", nextNumber);
        storageService.save(stdPolicy, getCurrentUser());

        stdPolicy.setProcessList(null);

        paymentService.save(user.getTenantId(), stdPolicy.getId(), installmentsDto);

        logger.info("Save process completed. policyNumber={}, premium={}", nextNumber, stdPolicy.getPremium());
        return stdPolicy;
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
        
        if (!policyData.getPolicyStatus().equals(PolicyStatus.QUOTE.name())) {
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

        //var version = versionManager.getLatestVersionByProductCode(productCode);

        //PolicyData updatedPolicy = storageService.update(policy, userData, version, policyNumber);
        logger.info("Policy updated. policyNumber={}", policyNumber);
        //return updatedPolicy;
        return null;
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

    private BigDecimal calculateTotalPremium(StdPolicy stdPolicy) {
        BigDecimal premium = BigDecimal.ZERO;
        InsuredObject insObject = stdPolicy.getInsuredObjects().get(0);

        for (Cover cover : insObject.getCovers()) {
            if (cover.getPremium() != null) {
                premium = premium.add(cover.getPremium());
            }
        }
        return premium;
    }

    private Map<String, Object> buildCelVariables(VariableContext varCtx) {
        Map<String, Object> variables = new HashMap<>();
        for (PvVarDefinition def : varCtx.getDefinitions()) {
            String code = def.getCode();
            variables.put(code, toCelActivationValue(varCtx, def));
        }
        varCtx.getValues().forEach((k, v) -> variables.putIfAbsent(k, decodeCelValue(v)));
        return variables;
    }

    private Object toCelActivationValue(VariableContext varCtx, PvVarDefinition def) {
        String code = def.getCode();
        BigDecimal numeric = varCtx.getDecimal(code);
        if (numeric != null) {
            return numeric;
        }
        Object raw = varCtx.get(code);
        if (isEmptyCelValue(raw) && isNumericPolicyVar(code, def)) {
            return BigDecimal.ZERO;
        }
        return raw;
    }

    private static Object decodeCelValue(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return value;
    }

    private static boolean isEmptyCelValue(Object raw) {
        if (raw == null) {
            return true;
        }
        if (raw instanceof String s) {
            return s.trim().isEmpty();
        }
        return false;
    }

    private static boolean isNumericPolicyVar(String code, PvVarDefinition def) {
        if (def.getType() == PvVarDefinition.Type.NUMBER) {
            return true;
        }
        String lower = code.toLowerCase();
        return lower.contains("premium") || lower.contains("suminsured") || lower.endsWith("_amount");
    }

    private List<ValidationError> runCelValidation(
            RuleType ruleType,
            AuthenticatedUser user,
            ProductVersionModel product,
            VariableContext varCtx) {
        logger.debug(
                "Running CEL validation. ruleType={}, productCode={}, lob={}, clientId={}",
                ruleType,
                product.getCode(),
                product.getLob(),
                requestContext.getClient());
        RuleValidationContext ctx = new RuleValidationContext(
                user.getTenantId(),
                user.getTenantCode(),
                product.getCode(),
                product.getLob(),
                requestContext.getClient(),
                buildCelVariables(varCtx));

        logger.info(
                "Calling ruleValidationService.processValidation, impl={}",
                ruleValidationService.getClass().getName());
        List<String> messages = ruleValidationService.processValidation(ruleType, ctx);
        logger.info("ruleValidationService.processValidation returned {} message(s)", messages.size());

        List<ValidationError> errors = new ArrayList<>();
        for (String msg : messages) {
            errors.add(new ValidationError("CEL_RULE", msg, null));
        }
        if (!errors.isEmpty()) {
            logger.warn(
                    "CEL validation produced errors. ruleType={}, productCode={}, count={}",
                    ruleType,
                    product.getCode(),
                    errors.size());
        } else {
            logger.debug("CEL validation passed. ruleType={}, productCode={}", ruleType, product.getCode());
        }
        return errors;
    }

    private void throwValidationErrors(String stage, List<ValidationError> errors) {
        logger.warn("CEL validation failed at {}. errors={}", stage, errors.size());
        List<ErrorModel.ErrorDetail> errorDetails = errors.stream()
                .map(err -> new ErrorModel.ErrorDetail(
                        ErrorConstants.DOMAIN_VALIDATION,
                        ErrorConstants.REASON_VALIDATION_FAILED,
                        err.getReason(),
                        err.getPath()))
                .collect(Collectors.toList());
        String errorMessage = stage + " failed: " + errors.stream()
                .map(ValidationError::getReason)
                .collect(Collectors.joining(", "));
        throw new BadRequestException(new ErrorModel(400, errorMessage, errorDetails));
    }
}

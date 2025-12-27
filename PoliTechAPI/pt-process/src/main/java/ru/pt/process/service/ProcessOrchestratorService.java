package ru.pt.process.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.db.PolicyStatus;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.payment.PaymentType;
import ru.pt.api.dto.process.Cover;
import ru.pt.api.dto.process.CoverInfo;
import ru.pt.api.dto.process.InsuredObject;
import ru.pt.api.dto.process.ValidatorType;
//import ru.pt.api.dto.product.LobModel;
//import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.versioning.Version;
import ru.pt.api.service.calculator.CalculatorService;
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

import ru.pt.api.utils.JsonSetter;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;
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

import ru.pt.api.dto.process.PolicyDTO;
import ru.pt.api.dto.process.ProcessList;
import ru.pt.api.dto.process.Deductible;

import java.util.Map;
import java.util.HashMap;

import ru.pt.process.utils.VariablesService;
@Component
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
    private final AdminUserManagementService userService;
    private final EmailGateService emailGateService;

    

    private PolicyDTO policyFromJson(String policy) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
            

        PolicyDTO policyDTO = null;
        try {
            policyDTO = objectMapper.readValue(policy, PolicyDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return policyDTO;
    }
    private String policyToJson(PolicyDTO policyDTO) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.registerModule(new JavaTimeModule());
            
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                        
            return objectMapper.writeValueAsString(policyDTO);
            
        } catch (JsonProcessingException e) {
            logger.error("Error serializing PolicyDTO to JSON: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error("Unexpected error serializing PolicyDTO to JSON: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PolicyDTO calculatePremium(PolicyDTO policyDTO, ProductVersionModel product, List<PvVar> vars) {

        InsuredObject insObject = preProcessService.getInsuredObject(policyDTO, product);

        preProcessService.enrichVariablesBeforeCalculation(insObject, vars);

        List<PvVar> calculated = calculatorService.runCalculator(
                product.getId(), product.getVersionNo(), insObject.getPackageCode(), vars
        );

        insObject = postProcessService.setCovers(insObject, calculated);
        
        policyDTO.getInsuredObjects().get(0).setCovers(insObject.getCovers());

        BigDecimal premium = countPremium(insObject);

        policyDTO.setPremium(premium);

        for (PvVar var : calculated) {
            logger.info("Var {} {}", var.getVarCode(), var.getVarValue());
        }

        return policyDTO;
    }

    @Override
    public String calculate(String policy) {


 /* 
        var projection = new JsonProjection(policy);

        var productCode = projection.getProductCode();

        var product = productService.getProductByCode(productCode, false);

        //LobModel lobModel = lobService.getByCode(product.getLob());
        List<PvVar> pvVars = product.getVars();

        policy = preProcessService.enrichPolicy(policy, product);

        List<PvVar> vars = preProcessService.evaluateAndEnrichVariables(policy, pvVars, productCode);

        List<ValidationError> errors = validatorService.validate(ValidatorType.QUOTE, product, vars);

        if (!errors.isEmpty()) {
            // TODO нормальную структуру под ошибки
            throw new BadRequestException(errors.stream()
                    .map(ValidationError::getReason)
                    .collect(Collectors.joining(","))
            );
        }

        InsuredObject insObject = preProcessService.getInsuredObject(policy, product);

        preProcessService.enrichVariablesBeforeCalculation(insObject, vars);

        List<PvVar> calculated = calculatorService.runCalculator(
                product.getId(), product.getVersionNo(), insObject.getPackageCode(), vars
        );

        insObject = postProcessService.setCovers(insObject, calculated);

        var setter = new JsonSetter(policy);

        setter.setObjectValue("insuredObject", insObject);
//        setter.getObjectValue("insuredObject").setObjectValue("covers", insObject.getCovers());

        Double premium = countPremium(insObject);

        setter.setRawValue("premium", premium.toString());

        logger.info("Result after calculation {}", setter.writeValue());

        for (PvVar var : calculated) {
            logger.info("Var {} {}", var.getVarCode(), var.getVarValue());
        }

        return setter.writeValue();
*/

        PolicyDTO policyDTO = policyFromJson(policy);

        /* Удалить возможный хлам */
        policyDTO.setPremium(null);

        String productCode = policyDTO.getProductCode();
        var product = productService.getProductByCode(productCode, false);

        //LobModel lobModel = lobService.getByCode(product.getLob());
        List<PvVar> pvVars = product.getVars();

        policyDTO = preProcessService.enrichPolicy(policyDTO, product);

        /* обогащенные данные */
        String policyJSON = policyToJson(policyDTO);

        List<PvVar> vars = preProcessService.evaluateAndEnrichVariables(policyJSON, pvVars, productCode);

        List<ValidationError> errors = validatorService.validate(ValidatorType.QUOTE, product, vars);

        if (!errors.isEmpty()) {
            // TODO нормальную структуру под ошибки
            throw new BadRequestException(errors.stream()
                    .map(ValidationError::getReason)
                    .collect(Collectors.joining(","))
            );
        }

        policyDTO = calculatePremium(policyDTO, product, vars);

        return policyToJson(policyDTO);

    }

    @Override
    public String save(String policy) {

        PolicyDTO policyDTO = policyFromJson(policy);

        policyDTO.setProcessList(new ProcessList(ProcessList.SAVE));
        

        /* Удалить возможный хлам */
        policyDTO.setPremium(null);

        String productCode = policyDTO.getProductCode();
        var product = productService.getProductByCode(productCode, false);

        //LobModel lobModel = lobService.getByCode(product.getLob());
        List<PvVar> pvVars = product.getVars();

        policyDTO = preProcessService.enrichPolicy(policyDTO, product);

        /* обогащенные данные */
        String policyJSON = policyToJson(policyDTO);

        List<PvVar> vars = preProcessService.evaluateAndEnrichVariables(policyJSON, pvVars, productCode);

        List<ValidationError> errors = validatorService.validate(ValidatorType.QUOTE, product, vars);
        errors.addAll(validatorService.validate(ValidatorType.SAVE, product, vars));

        if (!errors.isEmpty()) {
            // TODO нормальную структуру под ошибки
            throw new BadRequestException(errors.stream()
                    .map(ValidationError::getReason)
                    .collect(Collectors.joining(","))
            );
        }

        policyDTO = calculatePremium(policyDTO, product, vars);
        
        Map<String, Object> paramMap = new HashMap<>();
        for (PvVar pvVar : vars) {
            paramMap.put(pvVar.getVarCode(), pvVar.getVarValue());
        }

        String nextNumber = numberGeneratorService.getNextNumber(paramMap, productCode);
        policyDTO.setPolicyNumber(nextNumber);

        String ph_digest = VariablesService.getPhDigest(product.getPhType(), paramMap);
        String io_digest = VariablesService.getIoDigest(product.getIoType(), paramMap);

        policyDTO.getProcessList().setPhDigest(ph_digest);
        policyDTO.getProcessList().setIoDigest(io_digest);

        var userData = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));

//        String newPolicyJson = policyToJson(policyDTO);

        storageService.save(policyDTO, userData);

        policyDTO.setProcessList(null);
        String newPolicyJson = policyToJson(policyDTO);

        // TODO async send KID + draft print form

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
        // set policy status
        PolicyData policyData = storageService.getPolicyById(UUID.fromString(policyId));
        JsonSetter jsonSetter = new JsonSetter(policyData.getPolicy());
        jsonSetter.setRawValue("status", "PAID");
        policyData.setPolicy(jsonSetter.writeValue());
        policyData.setPolicyStatus(PolicyStatus.PAID);
        storageService.update(policyData);

        Client clientById = userService.getClientById(policyData.getPolicyIndex().getClientAccountId());
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

    @Override
    public PolicyData createPolicy(String policy) {
        var jsonProjection = new JsonProjection(policy);

        var productCode = jsonProjection.getProductCode();

        var version = versionManager.getLatestVersionByProductCode(productCode);

        var userData = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));

        var uuid = UUID.randomUUID();
        MdcWrapper.putId(uuid.toString());

        return storageService.save(policy, userData, version, uuid);
    }

    @Override
    public PolicyData updatePolicy(String policyNumber, String policy) {

        var userData = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));

        var policyData = storageService.getPolicyByNumber(policyNumber);
        if (policyData.getPolicyStatus() != PolicyStatus.NEW) {
            throw new BadRequestException("Can't update policy, bad policy status");
        }

        var policyIndex = policyData.getPolicyIndex();

        if (!policyIndex.getUserAccountId().equals(userData.getAccountId()) ||
                !policyIndex.getClientAccountId().equals(userData.getClientId())) {
            // TODO 403
            throw new BadRequestException("Unable to update policy");
        }

        var jsonProjection = new JsonProjection(policy);

        var productCode = jsonProjection.getProductCode();

        var version = versionManager.getLatestVersionByProductCode(productCode);

        return storageService.update(policy, userData, version, policyNumber);
    }

    @Override
    public PolicyData getPolicyById(UUID id) {
        var userData = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));

        var policyData = storageService.getPolicyById(id);

        var policyIndex = policyData.getPolicyIndex();

        if (!policyIndex.getUserAccountId().equals(userData.getAccountId()) ||
                !policyIndex.getClientAccountId().equals(userData.getClientId())) {
            // TODO 403
            throw new BadRequestException("Unable to update policy");
        }

        return policyData;
    }

    @Override
    public PolicyData getPolicyByNumber(String policyNumber) {
        var userData = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Unable to get current user from context"));

        var policyData = storageService.getPolicyByNumber(policyNumber);

        var policyIndex = policyData.getPolicyIndex();

        if (!policyIndex.getUserAccountId().equals(userData.getAccountId()) ||
                !policyIndex.getClientAccountId().equals(userData.getClientId())) {
            // TODO 403
            throw new BadRequestException("Unable to update policy");
        }

        return policyData;
    }

    private BigDecimal countPremium(InsuredObject insObject) {
        BigDecimal premium = BigDecimal.ZERO;

        for (Cover cover : insObject.getCovers()) {
            if (cover.getPremium() != null) {
                premium = premium.add(BigDecimal.valueOf(cover.getPremium()));
            }
        }
        return premium;
    }

}

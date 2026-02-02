package ru.pt.payments.service.youkassa;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.db.PolicyStatus;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.payment.PaymentType;
import ru.pt.api.dto.payment.PolicyPurchaseCallbackRequest;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.payment.PaymentClient;
import ru.pt.api.service.payment.PolicyPurchaseCallbackApi;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.ClientService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class YoukassaPaymentClient implements PaymentClient {

    private static final Logger log = LoggerFactory.getLogger(YoukassaPaymentClient.class);
    private static final String PAYMENT_GATE = "YOUKASSA";
    private static final String DEFAULT_API_BASE = "https://api.yookassa.ru/v3";
    private static final String PAYMENTS_SUFFIX = "/payments";
    private static final String DEFAULT_CURRENCY = "RUB";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SecurityContextHelper securityContextHelper;
    private final ClientService clientService;
    private final String paymentsEndpoint;
    private final StorageService storageService;
    private final String redirectBaseUrl;
    private final String redirectPath;
    private final PolicyPurchaseCallbackApi purchaseCallbackApi;

    public YoukassaPaymentClient(ObjectMapper objectMapper,
                                 SecurityContextHelper securityContextHelper,
                                 ClientService clientService,
                                 @Value("${payments.youkassa.api-url:" + DEFAULT_API_BASE + "}") String apiBaseUrl,
                                 StorageService storageService,
                                 @Value("${payments.youkassa.redirect-base-url:http://localhost:8080}") String redirectBaseUrl,
                                 @Value("${payments.youkassa.redirect-path:/api/v1/payments/youkassa/redirect}") String redirectPath,
                                 PolicyPurchaseCallbackApi purchaseCallbackApi) {
        this.storageService = storageService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.securityContextHelper = securityContextHelper;
        this.clientService = clientService;
        this.paymentsEndpoint = normalizeEndpoint(apiBaseUrl);
        this.redirectBaseUrl = redirectBaseUrl;
        this.redirectPath = redirectPath;
        this.purchaseCallbackApi = purchaseCallbackApi;
    }

    @Override
    public String getPaymentGate() {
        return PAYMENT_GATE;
    }

    @Override
    public PaymentData createPayment(PaymentData paymentData) {
        ClientConfiguration configuration = resolveClientConfiguration();

        validatePaymentType(paymentData.getPaymentType());
        if (paymentData.getAmount() == null) {
            throw new IllegalArgumentException("Amount is required for YooKassa payment");
        }
        if (requiresRedirect(paymentData.getPaymentType()) && !StringUtils.hasText(paymentData.getSuccessUrl())) {
            throw new IllegalArgumentException("Success URL is required for redirect payments");
        }

        if (PaymentType.CASH.equals(paymentData.getPaymentType())) {
            paymentData.setPaymentDate(ZonedDateTime.now());
            paymentData.setOrderId(UUID.randomUUID().toString());
            return paymentData;
        }

        Map<String, Object> requestBody = buildRequestBody(paymentData, configuration);
        HttpHeaders headers = buildHeaders(configuration, true);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate
                    .exchange(paymentsEndpoint, HttpMethod.POST, entity, String.class)
                    .getBody();

            if (!StringUtils.hasText(response)) {
                throw new IllegalStateException("YooKassa returned empty response");
            }

            PaymentData result = populatePaymentData(paymentData, response);
            if (StringUtils.hasText(result.getOrderId())) {
                storageService.setPaymentOrderId(result.getPolicyNumber(), result.getOrderId());
            }
            return result;
        } catch (RestClientException | JsonProcessingException ex) {
            log.error("Failed to create YooKassa payment: {}", ex.getMessage(), ex);
            throw new IllegalStateException("Failed to create YooKassa payment", ex);
        }
    }

    @Override
    public List<ValidationError> validate(String policyNumber, BigDecimal amount) {
        List<ValidationError> errors = new ArrayList<>();

        if (!StringUtils.hasText(policyNumber)) {
            errors.add(new ValidationError("policyNumber.required", "Policy number is required", "policyNumber"));
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ValidationError("amount.invalid", "Amount must be greater than zero", "amount"));
        }

        PolicyData policyByNumber = storageService.getPolicyByNumber(policyNumber);
        if (policyByNumber == null || !policyNumber.equals(policyByNumber.getPolicyNumber())) {
            errors.add(new ValidationError("policyNumber.invalid", "Policy number not found", "policyNumber"));
            return errors;
        }
        if (policyByNumber.getPolicyIndex().getStartDate().compareTo(ZonedDateTime.now()) < 0) {
            errors.add(new ValidationError("paymentDate", "can't pay for this policy - it's already started", "startDate"));
        }
        if (policyByNumber.getPolicyStatus().equals(PolicyStatus.IN_PAYMENT) || policyByNumber.getPolicyStatus().equals(PolicyStatus.PAID)) {
            errors.add(new ValidationError("policyStatus", "can't pay for this policy, incorrect status", "status"));
        }

        return errors;
    }

    private ClientConfiguration resolveClientConfiguration() {
        AuthenticatedUser currentUser = securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new IllegalStateException("User is not authenticated"));

        if (currentUser.getClientId() == null) {
            throw new IllegalStateException("Authenticated user does not have client context");
        }

        Client client = clientService.getClientById(currentUser.getClientId());
        ClientConfiguration configuration = client.getClientConfiguration();
        if (configuration == null) {
            throw new IllegalStateException("Client does not have payment configuration");
        }
        if (!PAYMENT_GATE.equalsIgnoreCase(configuration.getPaymentGate())) {
            throw new IllegalStateException("Client configured payment gate does not match YooKassa");
        }
        if (!StringUtils.hasText(configuration.getPaymentGateLogin()) ||
                !StringUtils.hasText(configuration.getPaymentGatePassword())) {
            throw new IllegalStateException("YooKassa credentials are not configured");
        }
        return configuration;
    }

    private Map<String, Object> buildRequestBody(PaymentData paymentData, ClientConfiguration configuration) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", Map.of(
                "value", paymentData.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString(),
                "currency", DEFAULT_CURRENCY
        ));
        payload.put("capture", Boolean.TRUE);
        payload.put("description", buildDescription(paymentData));
        payload.put("metadata", buildMetadata(paymentData, configuration));
        payload.put("payment_method_data", Map.of("type", resolvePaymentMethod(paymentData.getPaymentType())));
        payload.put("confirmation", buildConfirmation(paymentData));
        return payload;
    }

    private Map<String, Object> buildMetadata(PaymentData paymentData, ClientConfiguration configuration) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("policyNumber", paymentData.getPolicyNumber());
        metadata.put("installment", paymentData.getInstallmentNumber());
        if (StringUtils.hasText(paymentData.getFailUrl())) {
            metadata.put("failUrl", paymentData.getFailUrl());
        }
        if (StringUtils.hasText(configuration.getPaymentGateAgentNumber())) {
            metadata.put("agentNumber", configuration.getPaymentGateAgentNumber());
        }
        return metadata;
    }

    private Map<String, Object> buildConfirmation(PaymentData paymentData) {
        Map<String, Object> confirmation = new LinkedHashMap<>();
        PaymentType type = paymentData.getPaymentType() == null ? PaymentType.CARD : paymentData.getPaymentType();
        if (PaymentType.SBP.equals(type)) {
            confirmation.put("type", "qr");
        } else {
            confirmation.put("type", "redirect");
            confirmation.put("return_url", buildBackendRedirectUrl(paymentData));
        }
        return confirmation;
    }

    private String buildDescription(PaymentData paymentData) {
        if (!StringUtils.hasText(paymentData.getPolicyNumber())) {
            return "Оплата по договору";
        }
        return "Оплата по договору " + paymentData.getPolicyNumber();
    }

    private String resolvePaymentMethod(PaymentType paymentType) {
        if (paymentType == null || PaymentType.CARD.equals(paymentType)) {
            return "bank_card";
        }
        if (PaymentType.SBP.equals(paymentType)) {
            return "sbp";
        }
        throw new IllegalArgumentException("Payment type " + paymentType + " is not supported by YooKassa");
    }

    private PaymentData populatePaymentData(PaymentData paymentData, String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        paymentData.setOrderId(root.path("id").asText(null));

        JsonNode confirmationNode = root.path("confirmation");
        if (confirmationNode != null && !confirmationNode.isMissingNode()) {
            String confirmationUrl = confirmationNode.path("confirmation_url").asText(null);
            if (StringUtils.hasText(confirmationUrl)) {
                paymentData.setPaymentLink(confirmationUrl);
            }
            String confirmationData = confirmationNode.path("confirmation_data").asText(null);
            if (StringUtils.hasText(confirmationData)) {
                paymentData.setQrLink(confirmationData);
            }
        }

        String paidAt = root.path("paid_at").asText(null);
        if (!StringUtils.hasText(paidAt)) {
            paidAt = root.path("created_at").asText(null);
        }
        if (StringUtils.hasText(paidAt)) {
            try {
                paymentData.setPaymentDate(ZonedDateTime.parse(paidAt));
            } catch (DateTimeParseException ignored) {
                log.debug("Unable to parse YooKassa datetime {}", paidAt);
            }
        }

        return paymentData;
    }

    private HttpHeaders buildHeaders(ClientConfiguration configuration, boolean withIdempotence) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.AUTHORIZATION, buildBasicAuth(configuration));
        if (withIdempotence) {
            headers.set("Idempotence-Key", UUID.randomUUID().toString());
        }
        return headers;
    }

    private String buildBasicAuth(ClientConfiguration configuration) {
        String credentials = configuration.getPaymentGateLogin() + ":" + configuration.getPaymentGatePassword();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private void validatePaymentType(PaymentType paymentType) {
        if (paymentType == null || PaymentType.CARD.equals(paymentType) || PaymentType.SBP.equals(paymentType)) {
            return;
        }
        throw new IllegalArgumentException("Payment type " + paymentType + " is not supported by YooKassa");
    }

    private boolean requiresRedirect(PaymentType paymentType) {
        return paymentType == null || PaymentType.CARD.equals(paymentType);
    }

    private String normalizeEndpoint(String apiBaseUrl) {
        String base = StringUtils.hasText(apiBaseUrl) ? apiBaseUrl : DEFAULT_API_BASE;
        if (base.endsWith(PAYMENTS_SUFFIX)) {
            return base;
        }
        if (base.endsWith("/")) {
            return base + "payments";
        }
        return base + PAYMENTS_SUFFIX;
    }

    private String buildBackendRedirectUrl(PaymentData paymentData) {
        if (!StringUtils.hasText(paymentData.getSuccessUrl())) {
            throw new IllegalArgumentException("Success URL is required for redirect confirmations");
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(redirectBaseUrl)
                .path(redirectPath)
                .queryParam("successTarget", paymentData.getSuccessUrl());

        if (StringUtils.hasText(paymentData.getFailUrl())) {
            builder.queryParam("failTarget", paymentData.getFailUrl());
        }

        if (StringUtils.hasText(paymentData.getPolicyNumber())) {
            builder.queryParam("policyNumber", paymentData.getPolicyNumber());
        }
        if (paymentData.getInstallmentNumber() != null) {
            builder.queryParam("installment", paymentData.getInstallmentNumber());
        }

        return builder.build(true).toUriString();
    }

    private void handlePaymentStatus(String paymentId, String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        String status = root.path("status").asText("");
        if (!"succeeded".equalsIgnoreCase(status)) {
            log.info("Payment {} status is {}, ignoring callback", paymentId, status);
            return;
        }

        PolicyData policyData = storageService.getPolicyByPaymentOrderId(paymentId);
        PolicyPurchaseCallbackRequest request = new PolicyPurchaseCallbackRequest();
        request.setPaymentId(paymentId);
        request.setPolicyNumber(policyData.getPolicyNumber());
        request.setPaymentDate(parsePaymentDate(root));
        request.setPaidAmount(parseAmount(root));
        request.setPolicyHolderEmail(extractPolicyHolderEmail(policyData.getPolicy()));

        purchaseCallbackApi.handlePolicyPurchase(request);
    }

    private ZonedDateTime parsePaymentDate(JsonNode root) {
        String paidAt = root.path("paid_at").asText(null);
        if (!StringUtils.hasText(paidAt)) {
            paidAt = root.path("captured_at").asText(null);
        }
        if (StringUtils.hasText(paidAt)) {
            try {
                return ZonedDateTime.parse(paidAt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return ZonedDateTime.now();
    }

    private BigDecimal parseAmount(JsonNode root) {
        JsonNode valueNode = root.path("amount").path("value");
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }
        if (valueNode.isNumber()) {
            return valueNode.decimalValue();
        }
        try {
            return new BigDecimal(valueNode.asText());
        } catch (NumberFormatException ex) {
            log.warn("Unable to parse amount from YooKassa payload: {}", valueNode.asText());
            return null;
        }
    }

    private String extractPolicyHolderEmail(String policyJson) {
        if (!StringUtils.hasText(policyJson)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(policyJson);
            JsonNode emailNode = root.path("policyHolder").path("email");
            if (emailNode.isMissingNode() || emailNode.isNull()) {
                return null;
            }
            return emailNode.asText(null);
        } catch (Exception ex) {
            log.warn("Unable to extract policy holder email", ex);
            return null;
        }
    }
}

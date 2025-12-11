package ru.pt.payments.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.db.PolicyStatus;
import ru.pt.api.dto.payment.PolicyPurchaseCallbackRequest;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.payment.PolicyPurchaseCallbackApi;

@Service
public class PolicyPurchaseCallbackService implements PolicyPurchaseCallbackApi {

    private static final Logger log = LoggerFactory.getLogger(PolicyPurchaseCallbackService.class);

    private final StorageService storageService;
    private final PolicyPurchaseEmailSender emailSender;
    private final ObjectMapper objectMapper;

    public PolicyPurchaseCallbackService(StorageService storageService,
                                         PolicyPurchaseEmailSender emailSender,
                                         ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.emailSender = emailSender;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void handlePolicyPurchase(PolicyPurchaseCallbackRequest request) {
        if (request == null || request.getPolicyNumber() == null) {
            throw new IllegalArgumentException("Policy purchase callback request is invalid");
        }

        PolicyData policyData = storageService.getPolicyByNumber(request.getPolicyNumber());
        policyData.setPolicyStatus(PolicyStatus.PAID);
        storageService.update(policyData);

        log.info("Policy {} marked as PAID after payment {}", request.getPolicyNumber(), request.getPaymentId());

        if (!StringUtils.hasText(request.getPolicyHolderEmail())) {
            request.setPolicyHolderEmail(extractPolicyHolderEmail(policyData.getPolicy()));
        }

        if (StringUtils.hasText(request.getPolicyHolderEmail())) {
            emailSender.sendPolicyPurchasedEmail(request);
        } else {
            log.warn("Policy holder email is missing for policy {}", request.getPolicyNumber());
        }
    }

    private String extractPolicyHolderEmail(String policyJson) {
        if (!StringUtils.hasText(policyJson)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(policyJson);
            JsonNode emailNode = node.path("policyHolder").path("email");
            if (!emailNode.isMissingNode() && !emailNode.isNull()) {
                return emailNode.asText(null);
            }
        } catch (Exception ex) {
            log.warn("Failed to extract policy holder email", ex);
        }
        return null;
    }
}


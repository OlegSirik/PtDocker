package ru.pt.payments.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.payment.PolicyPurchaseCallbackRequest;

/**
 * Placeholder email sender. In real life this would call
 * notification service or SMTP provider.
 */
@Component
public class PolicyPurchaseEmailSender {

    private static final Logger log = LoggerFactory.getLogger(PolicyPurchaseEmailSender.class);

    public void sendPolicyPurchasedEmail(PolicyPurchaseCallbackRequest request) {
        log.info("Sending policy purchase email to {} for policy {} amount {}",
                request.getPolicyHolderEmail(),
                request.getPolicyNumber(),
                request.getPaidAmount());
        // TODO integrate with actual email service
    }
}


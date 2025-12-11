package ru.pt.api.service.payment;

import ru.pt.api.dto.payment.PolicyPurchaseCallbackRequest;

/**
 * API for handling callbacks received after a customer successfully buys a policy.
 * Implementations are responsible for triggering follow-up actions such as
 * sending confirmation emails to the policy holder.
 */
public interface PolicyPurchaseCallbackApi {

    /**
     * Handle policy purchase event.
     * Expected to send email notifications to the policy holder
     * and perform additional business logic specific to the platform.
     *
     * @param request payload that describes the completed purchase
     */
    void handlePolicyPurchase(PolicyPurchaseCallbackRequest request);
}


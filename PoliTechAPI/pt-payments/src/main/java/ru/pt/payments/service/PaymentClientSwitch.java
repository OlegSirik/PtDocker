package ru.pt.payments.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.service.payment.PaymentClient;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.Map;

@Component
public class PaymentClientSwitch {

    private final Map<String, PaymentClient> availableClients;
    private final AdminUserManagementService userService;
    private final SecurityContextHelper securityContextHelper;

    public PaymentClientSwitch(Map<String, PaymentClient> availableClients,
                               AdminUserManagementService userService,
                               SecurityContextHelper securityContextHelper) {
        this.availableClients = availableClients;
        this.userService = userService;
        this.securityContextHelper = securityContextHelper;
    }

    public PaymentClient getPaymentClient(UserDetailsImpl userDetails) {
        return resolveClient(userDetails.getClientId());
    }

    public PaymentClient getCurrentPaymentClient() {
        UserDetailsImpl currentUser = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("User is not authenticated"));
        return resolveClient(currentUser.getClientId());
    }

    private PaymentClient resolveClient(Long clientId) {
        if (clientId == null) {
            throw new IllegalStateException("Client id is required to resolve payment gate");
        }
        Client client = userService.getClientById(clientId);
        if (client.getClientConfiguration() == null) {
            throw new IllegalStateException("No payment configuration provided! You need to configure it first");
        }

        String paymentGate = client.getClientConfiguration().getPaymentGate();
        if (paymentGate == null || !availableClients.containsKey(paymentGate)) {
            throw new IllegalStateException("Incorrect payment gate configured, fix it first!");
        }

        return availableClients.get(paymentGate);
    }
}

package ru.pt.payments.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.service.payment.PaymentClient;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.ClientService;

import java.util.Map;

@Component
public class PaymentClientSwitch {

    private final Map<String, PaymentClient> availableClients;
    private final ClientService clientService;
    private final SecurityContextHelper securityContextHelper;

    public PaymentClientSwitch(Map<String, PaymentClient> availableClients,
                               ClientService clientService,
                               SecurityContextHelper securityContextHelper) {
        this.availableClients = availableClients;
        this.clientService = clientService;
        this.securityContextHelper = securityContextHelper;
    }

    public PaymentClient getPaymentClient(AuthenticatedUser userDetails) {
        return resolveClient(userDetails.getClientId());
    }

    public PaymentClient getCurrentPaymentClient() {
        AuthenticatedUser currentUser = securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new IllegalStateException("User is not authenticated"));
        return resolveClient(currentUser.getClientId());
    }

    private PaymentClient resolveClient(Long clientId) {
        if (clientId == null) {
            throw new IllegalStateException("Client id is required to resolve payment gate");
        }
        Client client = clientService.getClientById(clientId);
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

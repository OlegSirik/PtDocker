package ru.pt.payments.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.pt.api.service.payment.PaymentClient;
import ru.pt.api.service.payment.PolicyPurchaseCallbackApi;
import ru.pt.payments.service.PolicyPurchaseCallbackService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@ComponentScan("ru.pt.payments")
public class PaymentModuleConfiguration {

    @Bean
    public Map<String, PaymentClient> availableClients(List<PaymentClient> clientList) {
        return clientList.stream()
                .collect(Collectors.toMap(PaymentClient::getPaymentGate, Function.identity()));
    }

    @Bean
    public PolicyPurchaseCallbackApi policyPurchaseCallbackApi(PolicyPurchaseCallbackService service) {
        return service;
    }
}

package ru.pt.payments.service.vsk.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.pt.payments.model.vsk.IdentifiedPayerModel;
import ru.pt.payments.model.vsk.PaymentKafkaResponse;
import ru.pt.payments.model.vsk.PaymentRequest;
import ru.pt.payments.model.vsk.UnidentifiedPayerModel;

import java.time.Duration;
import java.util.Collections;

@Component
public class VskPaymentApiClient implements VskPaymentApi {

    private final String rootUrl;
    private final RestTemplate restTemplate;

    public VskPaymentApiClient(
            @Value("${payments.vsk.rootUrl}") String rootUrl,
            @Value("${payments.vsk.connectTimeoutSec:10}") Long connectTimeout,
            @Value("${payments.vsk.readTimeoutSec:110}") Long readTimeout,
            RestTemplateBuilder restTemplateBuilder) {
        this.rootUrl = rootUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(connectTimeout))
                .setReadTimeout(Duration.ofSeconds(readTimeout))
                .build();
    }


    @Override
    public String identifiedPayer(IdentifiedPayerModel identifiedPayerModel) {
        return restTemplate.exchange(
                rootUrl + "/api/v1/individual/identified",
                HttpMethod.POST,
                new HttpEntity<>(identifiedPayerModel),
                String.class
        ).getBody();
    }

    @Override
    public String unidentifiedPayer(UnidentifiedPayerModel unidentifiedPayerModel) {
        return restTemplate.exchange(
                rootUrl + "/api/v1/individual/unidentified",
                HttpMethod.POST,
                new HttpEntity<>(unidentifiedPayerModel),
                String.class
        ).getBody();
    }

    @Override
    public PaymentKafkaResponse createPayment(PaymentRequest paymentRequest,
                                              String draftId,
                                              String callbackQueue,
                                              Boolean legacySystem) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.put("draftId", Collections.singletonList(draftId));
        headers.put("systemIdentifier", Collections.singletonList(callbackQueue));
        headers.put("isLegacySystem", Collections.singletonList(legacySystem.toString()));

        HttpEntity<PaymentRequest> httpEntity = new HttpEntity<>(paymentRequest, headers);
        return restTemplate.exchange(
                rootUrl + "/api/v3",
                HttpMethod.POST,
                httpEntity,
                PaymentKafkaResponse.class
        ).getBody();
    }
}

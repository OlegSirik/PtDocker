package ru.pt.payments.service.vsk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.errors.ValidationError;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.payment.PaymentType;
import ru.pt.api.service.db.StorageService;
import ru.pt.api.service.payment.PaymentClient;
import ru.pt.api.utils.JsonProjection;
import ru.pt.payments.model.vsk.*;
import ru.pt.payments.service.vsk.api.VskPaymentApi;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class VskPaymentClient implements PaymentClient {

    private static final String PAYMENT_GATE = "VSK";

    private final Logger logger = LoggerFactory.getLogger(VskPaymentClient.class);

    private final VskPaymentApi vskPaymentApi;
    private final StorageService storageService;

    public VskPaymentClient(VskPaymentApi vskPaymentApi, StorageService storageService) {
        this.vskPaymentApi = vskPaymentApi;
        this.storageService = storageService;
    }

    @Override
    public String getPaymentGate() {
        return PAYMENT_GATE;
    }

    @Override
    public PaymentData createPayment(PaymentData paymentData) {
        PaymentType paymentType = paymentData.getPaymentType();
        if (PaymentType.CASH.equals(paymentType)) {
            paymentData.setPaymentDate(ZonedDateTime.now());
            paymentData.setOrderId(UUID.randomUUID().toString());
            return paymentData;
        }
        PaymentRequest paymentRequest = new PaymentRequest();

        String policyRaw = storageService.getPolicyByNumber(paymentData.getPolicyNumber()).getPolicy();
        JsonProjection jsonProjection = new JsonProjection(policyRaw);
        paymentRequest.setTypeId("613c8940-5816-4dfa-9e0b-08addea3bdae");
        paymentRequest.setCurrencyId("F87F6E8B-85B2-424D-A0E1-7DE1B4611A2A");

        PaymentRequestAttributes paymentRequestAttributes = new PaymentRequestAttributes();
        paymentRequestAttributes.setPayerEmail(jsonProjection.getEmail());
        paymentRequestAttributes.setPayerPhone(jsonProjection.getPhone());
        paymentRequestAttributes.setPayerTypeId("a8298ec2-959c-4136-9c57-cc88b4b4c76a");
        paymentRequestAttributes.setSourceId("56c3b5f5-bd52-4979-8a68-b1b5c5be51a5");
        // TODO vsk_id
        String vskId;
        try {
            UnidentifiedPayerModel unidentifiedPayerModel = new UnidentifiedPayerModel();
            unidentifiedPayerModel.setSystemId("partapi");
            PersonDataModel personDataModel = new PersonDataModel();
            PrimaryPersonDataModel primaryPersonDataModel = new PrimaryPersonDataModel();
            primaryPersonDataModel.setBirthDate(toDateModel(jsonProjection.getBirthDate()));
            primaryPersonDataModel.setFirstName(jsonProjection.getFirstName());
            primaryPersonDataModel.setLastName(jsonProjection.getLastName());
            primaryPersonDataModel.setMiddleName(jsonProjection.getMiddleName());
            primaryPersonDataModel.setNoMiddleNameFlag(primaryPersonDataModel.getMiddleName() == null);
            personDataModel.setPrimaryPersonDataModel(primaryPersonDataModel);
            unidentifiedPayerModel.setPersonDataModel(personDataModel);

            vskId = vskPaymentApi.unidentifiedPayer(unidentifiedPayerModel);
        } catch (Exception e) {
            logger.error("Unable to get person vsk id {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        paymentRequestAttributes.setPayerId(vskId);


        paymentRequest.setAttributes(paymentRequestAttributes);

        paymentRequest.setLots(buildLots(paymentData));

        paymentRequest.setItems(buildItems(jsonProjection, paymentData));

        PaymentKafkaResponse response = vskPaymentApi.createPayment(
                paymentRequest,
                jsonProjection.getPolicyId().toString(),
                "partapi-platform",
                true
        );

        if (paymentType.equals(PaymentType.CARD)) {
            paymentData.setPaymentLink(response.getPaymentLink());
        } else if (paymentType.equals(PaymentType.SBP)) {
            paymentData.setQrLink(response.getPaymentQRData());
        } else if (paymentType.equals(PaymentType.CASHLESS)) {
            paymentData.setBillBase64(downloadBill(response.getPaymentLink()));
        }

        return paymentData;
    }

    private byte[] downloadBill(String link) {
        try {
            return new URL(link).openStream().readAllBytes();
        } catch (IOException e) {
            logger.error("Unable to get bill by link - {}", link, e);
            throw new RuntimeException(e);
        }
    }

    private List<Lot> buildLots(PaymentData paymentData) {
        PaymentType paymentType = paymentData.getPaymentType();
        Lot lot = new Lot();
        lot.setCost(paymentData.getAmount());
        lot.setTypeId(
                switch (paymentType) {
                    case SBP -> "c03df27e-2f0e-477c-8c60-1df3158a1b00";
                    case CARD -> "78546176-6e37-4afd-9dfe-21e86677b9f8";
                    case CASH -> "7327071f-e3db-455b-9bb4-b1af8da7efd0";
                    case CASHLESS -> "c758e034-be10-49e3-adb8-69bc367bbc63";
                }
        );
        if (!PaymentType.CASH.equals(paymentType)) {
            LotAttributes attributes = new LotAttributes();
            attributes.setFailUrl(paymentData.getFailUrl());
            attributes.setReturnUrl(paymentData.getSuccessUrl());
            lot.setAttributes(attributes);
        }
        return Collections.singletonList(lot);
    }

    public List<Item> buildItems(JsonProjection jsonProjection,
                                 PaymentData paymentData) {
        Item item = new Item();
        item.setCost(paymentData.getAmount().toPlainString());
        item.setTypeId("CA22E0B7-1E65-4D2A-A1CE-EE1960C32B9A");
        item.setDocumentTypeId("e4f26160-ba99-4f62-9502-2fcd7194bef0");
        ItemAttributes attributes = new ItemAttributes();
        attributes.setDocumentNumber(jsonProjection.getPolicyNumber());
        item.setAttributes(attributes);
        item.setDocumentId(jsonProjection.getPolicyId().toString());
        return Collections.singletonList(item);
    }

    private DateModel toDateModel(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        } else {
            return new DateModel(birthDate.getYear(), birthDate.getMonthValue(), birthDate.getDayOfMonth());
        }
    }

    @Override
    public void paymentCallback(String policyId) {

    }

    @Override
    public List<ValidationError> validate(String policyNumber, BigDecimal amount) {
        return List.of();
    }
}

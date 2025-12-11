package ru.pt.payments.service.vsk.api;

import ru.pt.payments.model.vsk.IdentifiedPayerModel;
import ru.pt.payments.model.vsk.PaymentKafkaResponse;
import ru.pt.payments.model.vsk.PaymentRequest;
import ru.pt.payments.model.vsk.UnidentifiedPayerModel;

public interface VskPaymentApi {

    /**
     * Получение идентификатора плательщика в системе ВСК
     * Для идентифицированных плательщиков - полис будет видно в ЛК ВСК
     *
     * @param identifiedPayerModel данные для идентификации
     * @return айди плательщика
     */
    String identifiedPayer(IdentifiedPayerModel identifiedPayerModel);

    /**
     * Получение идентификатора плательщика в системе ВСК
     * Для неидентифицированных плательщиков - полис не будет видно в ЛК ВСК
     *
     * @param unidentifiedPayerModel данные для идентификации
     * @return айди плательщика
     */
    String unidentifiedPayer(UnidentifiedPayerModel unidentifiedPayerModel);

    /**
     * Создать платеж в системе ВСК
     *
     * @param paymentRequest запрос на оплату
     * @param draftId        идентификатор процесса
     * @param callbackQueue  очередь для получения подтверждения оплаты
     * @param legacySystem   нужно ли сервису подтверждать оплату
     * @return ответ с платежными данными
     */
    PaymentKafkaResponse createPayment(
            PaymentRequest paymentRequest,
            String draftId,
            String callbackQueue,
            Boolean legacySystem
    );

}

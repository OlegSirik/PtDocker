package ru.pt.api.service.process;

import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.payment.PaymentData;
import ru.pt.api.dto.policy.StdPolicy;

import java.util.UUID;

/**
 * Описание процессов, в которых участвует договор
 */
public interface ProcessOrchestrator {
    /**
     * Расчёт котировки (формат-независимый вход).
     * Маппинг wire JSON → {@link StdPolicy} выполняется на уровне API-контроллера.
     */
    StdPolicy quote(StdPolicy policy);

    /**
     * Оформление договора (формат-независимый вход).
     * Маппинг wire JSON → {@link StdPolicy} выполняется на уровне API-контроллера.
     */
    StdPolicy save(StdPolicy policy);

    /**
     * Обновить неоплаченный полис
     * @param policy полис
     * @return полис с премией и номером и графиком платежей
     */
    String update(String policy);

    /**
     * Создать ДС для оплаченного полиса
     * @param policy полис
     * @return полис с доплатой или без
     */
    String createAddendum(String policy);

    /**
     * Оплата
     * @param paymentData данные для оплаты
     * @return ссылка на оплату/QR код/время оплаты/чек
     */
    PaymentData payment(PaymentData paymentData);

    /**
     * Действия после подтверждения оплаты
     * Разные платежные клиенты могут по разному триггерить вызов этого метода
     * @param policyId ИДЕНТИФИКАТОР ПОЛИСА(draftId)
     */
    void paymentCallback(String policyId);

    /**
     * Сохранить договор в хранилище
     * @param policy договор
     * @return данные после сохранения
     */
    //PolicyData createPolicy(String policy);

    /**
     * Обновить договор в хранилище
     * @param policy договор
     * @param policyNumber номер договора
     * @return данные после сохранения
     */
    PolicyData updatePolicy(String policyNumber, String policy);

    /**
     * Получить полис по айди с проверкой принадлежности полиса пользователю
     * @param id айди полиса
     * @return данные по полису
     */
    PolicyData getPolicyById(UUID id);

    /**
     * Получить полис по номеру с проверкой принадлежности полиса пользователю
     * @param policyNumber номер полиса
     * @return данные по полису
     */
    PolicyData getPolicyByNumber(String policyNumber);
}

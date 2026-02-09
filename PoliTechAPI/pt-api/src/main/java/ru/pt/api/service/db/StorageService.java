package ru.pt.api.service.db;

import ru.pt.api.dto.db.PolicyData;
import ru.pt.api.dto.versioning.Version;
import ru.pt.api.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

import ru.pt.api.dto.process.PolicyDTO;
/**
 * Сервис хранения договоров + методы доступа
 */
public interface StorageService {

    /**
     * Сохранить полис
     * @param policy договор
     * @param userData данные о пользователе
     * @param version версия под которой создан договор
     * @param uuid идентификатор договора/процесса
     * @return сохраненный договор
     */
    //PolicyData save(String policy, AuthenticatedUser userData, Version version, UUID uuid);

    /**
     * Сохранить полис
     * @param policy договор
     * @param userData данные о пользователе
     * @return сохраненный договор
     */
    PolicyData save(PolicyDTO policy, AuthenticatedUser userData);
    /**
     * Обновить договор целиком
     * @param policyData данные договора для сохранения
     */
    void update(PolicyData policyData);

    /**
     * Обновить полис в статусе NEW, оплаченные не обновляем
     * Между тем это интерфейс хранилища - проверка статуса должна быть перед вызовом этого метода
     * @param policy полис
     * @param userData данные о пользователе
     * @param version версия под которой был создан ИЗНАЧАЛЬНЫЙ договор
     * @param policyNumber номер полиса для обновления
     * @return обновленный договор
     */
    PolicyData update(String policy, AuthenticatedUser userData, Version version, String policyNumber);

    /**
     * Получить данные по айди договора
     * @param policyId UUID договора
     * @return договор
     */
    PolicyData getPolicyById(UUID policyId);

    /**
     * Получить данные по номеру договора
     * @param policyNumber номер договора
     * @return договор
     */
    PolicyData getPolicyByNumber(String policyNumber);

    /**
     * Получить все договоры пользователя
     * @return список договоров
     */
    List<PolicyData> getPoliciesForUser();
    // TODO методы для фильтрации списка полисов

    /**
     * Сохранить идентификатор платежа, чтобы связать уведомления от Юкассы
     * с конкретным полисом.
     * @param policyNumber номер полиса
     * @param paymentOrderId идентификатор платежа в YooKassa
     */
    void setPaymentOrderId(String policyNumber, String paymentOrderId);

    /**
     * Найти полис по идентификатору платежа.
     * @param paymentOrderId идентификатор платежа в YooKassa
     * @return данные полиса
     */
    PolicyData getPolicyByPaymentOrderId(String paymentOrderId);
}

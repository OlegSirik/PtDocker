package ru.pt.api.service.product;

import ru.pt.api.dto.product.InsuranceCompanyDto;

import java.util.List;

/**
 * Сервис страховых компаний (справочник для продукта и процессов).
 * <p>
 * Данные хранятся в {@code pt_insurance_company} (или аналоге) с привязкой к тенанту ({@code tid}).
 * Параметр {@code tenantId} задаёт область данных; реализация должна согласовывать его с текущим
 * тенантом из контекста безопасности ({@link ru.pt.api.security.AuthenticatedUser#getTenantId()}),
 * иначе — отказ в доступе. Значение {@code tenantCode} из URL контроллера не подставляется автоматически.
 * <p>
 * Авторизация: ресурс {@link ru.pt.api.service.auth.AuthZ.ResourceType#INS_COMPANY}, действия
 * {@code LIST} / {@code VIEW} / {@code MANAGE} в зависимости от метода.
 */
public interface InsCompanyService {

    /**
     * Создать страховую компанию в указанном тенанте.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param dto поля компании; обязательны непустые {@code code} и {@code name};
     *            {@code code} уникален в пределах тенанта; {@code status} — {@code active} или {@code suspended}
     *            (по умолчанию {@code active})
     * @return сохранённая сущность с присвоенным {@code id}
     * @throws ru.pt.api.dto.exception.BadRequestException при невалидных данных или дубле {@code code}
     */
    InsuranceCompanyDto create(Long tenantId, InsuranceCompanyDto dto);

    /**
     * Обновить существующую страховую компанию в указанном тенанте.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param dto тело с {@code id}; путь API обычно подставляет {@code id} из URL
     * @return обновлённый DTO
     * @throws ru.pt.api.dto.exception.BadRequestException если {@code id} отсутствует, нарушены ограничения полей
     * @throws ru.pt.api.dto.exception.NotFoundException если запись не найдена для данного тенанта
     */
    InsuranceCompanyDto update(Long tenantId, InsuranceCompanyDto dto);

    /**
     * Удалить страховую компанию по идентификатору (жёсткое удаление в БД в текущей реализации).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id первичный ключ
     * @throws ru.pt.api.dto.exception.NotFoundException если запись не найдена для данного тенанта
     */
    void delete(Long tenantId, Long id);

    /**
     * Получить одну страховую компанию по {@code id} в рамках указанного тенанта.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id первичный ключ
     * @return DTO
     * @throws ru.pt.api.dto.exception.NotFoundException если не найдено
     */
    InsuranceCompanyDto get(Long tenantId, Long id);

    /**
     * Список всех страховых компаний указанного тенанта, сортировка по {@code code} (по возрастанию).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @return список (может быть пустым)
     */
    List<InsuranceCompanyDto> getAll(Long tenantId);
}

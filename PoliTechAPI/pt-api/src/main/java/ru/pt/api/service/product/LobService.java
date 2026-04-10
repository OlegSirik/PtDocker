package ru.pt.api.service.product;

import ru.pt.api.dto.product.LobModel;

import java.util.List;

/**
 * Сервис линий бизнеса (LOB): шаблон продукта, покрытия, переменные, схема договора.
 * <p>
 * Данные хранятся в {@code pt_lobs} (JSON-модель в колонке {@code lob}) с привязкой к тенанту ({@code tid}).
 * Параметр {@code tenantId} задаёт область данных; реализация должна согласовывать его с текущим
 * тенантом из контекста безопасности ({@link ru.pt.api.security.AuthenticatedUser#getTenantId()}),
 * иначе — отказ в доступе.
 * <p>
 * Авторизация: ресурс {@link ru.pt.api.service.auth.AuthZ.ResourceType#LOB}, действия
 * {@code LIST} / {@code VIEW} / {@code MANAGE} в зависимости от метода.
 */
public interface LobService {

    /**
     * Список активных (не удалённых) LOB тенанта — краткие сводки для списков и выбора.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @return краткие описания LOB (код, наименование, идентификатор)
     */
    List<LobModel> listActiveSummaries(Long tenantId);

    /**
     * Найти LOB по коду в рамках тенанта.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param code     код линии бизнеса ({@code mpCode})
     * @return полная модель LOB или {@code null}, если не найдено
     */
    LobModel getByCode(Long tenantId, String code);

    /**
     * Найти LOB по идентификатору в рамках тенанта.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id       первичный ключ LOB
     * @return полная модель LOB или {@code null}, если не найдено
     */
    LobModel getById(Long tenantId, Long id);

    /**
     * Создать новую линию бизнеса.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param payload  описание LOB; обязательны непустые {@code mpCode} и {@code mpName}; код уникален в тенанте
     * @return сохранённая модель с присвоенным {@code id}
     * @throws ru.pt.api.dto.exception.BadRequestException при дубле кода, пустых полях или дубликатах {@code varCode}/{@code coverCode}
     */
    LobModel create(Long tenantId, LobModel payload);

    /**
     * Мягко удалить LOB (отметка удаления), запись остаётся в БД.
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id       идентификатор LOB
     * @return {@code true}, если отметка удаления установлена
     * @throws ru.pt.api.dto.exception.NotFoundException если LOB не найден для данного тенанта
     */
    boolean softDeleteById(Long tenantId, Long id);

    /**
     * Обновить существующую LOB (по коду из {@code payload}).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param payload  новое описание; должен соответствовать существующей записи
     * @return обновлённая модель
     * @throws ru.pt.api.dto.exception.BadRequestException при нарушении ограничений данных
     * @throws ru.pt.api.dto.exception.NotFoundException если LOB не найдена
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException при некорректной структуре {@code mpVars} (например, covers)
     */
    LobModel update(Long tenantId, LobModel payload);
}

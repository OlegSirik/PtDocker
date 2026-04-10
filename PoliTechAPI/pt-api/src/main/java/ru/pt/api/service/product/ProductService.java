package ru.pt.api.service.product;

import ru.pt.api.dto.product.Product;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvVar;

import java.util.List;

/**
 * Сервис продуктов и их версий (конфигурация расчёта, переменные, публикация в PROD).
 * <p>
 * Данные версий хранятся в {@code pt_products} / {@code pt_product_versions} (модель в JSON) с привязкой к тенанту.
 * Параметр {@code tenantId} ({@code tid}) задаёт область данных; реализация согласует его с тенантом из контекста
 * безопасности ({@link ru.pt.api.security.AuthenticatedUser#getTenantId()}), иначе — отказ в доступе.
 * <p>
 * Авторизация: ресурс {@link ru.pt.api.service.auth.AuthZ.ResourceType#PRODUCT}, действия
 * {@code LIST}, {@code VIEW}, {@code MANAGE}, при необходимости {@code TEST}, {@code GO2PROD} — в зависимости от операции.
 */
public interface ProductService {

    /**
     * Краткий список продуктов тенанта (сводки для UI и выбора).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param insComp  идентификатор страховой компании ({@code pt_insurance_company.id}); {@code null} — все продукты
     * @return список (может быть пустым)
     */
    List<Product> listSummaries(Long tenantId, Long insComp);

    /**
     * Создать продукт с начальной dev-версией.
     *
     * @param tenantId            идентификатор тенанта ({@code tid})
     * @param productVersionModel описание продукта и первой версии
     * @return сохранённая версия продукта
     * @throws ru.pt.api.dto.exception.BadRequestException при невалидных обязательных полях ({@code lob}, {@code code}, {@code name} и т.д.)
     */
    ProductVersionModel create(Long tenantId, ProductVersionModel productVersionModel);

    /**
     * Опубликовать указанную dev-версию в прод (PROD).
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта
     * @param versionNo номер публикуемой версии
     * @return опубликованная (prod) версия
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если нет подходящей dev-версии для публикации
     */
    ProductVersionModel publishToProd(Long tenantId, Long productId, Long versionNo);

    /**
     * Получить конкретную версию продукта по идентификатору продукта и номеру версии.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param id        идентификатор продукта
     * @param versionNo номер версии
     * @return полное описание версии
     * @throws ru.pt.api.dto.exception.NotFoundException если версия не найдена
     */
    ProductVersionModel getVersion(Long tenantId, Long id, Long versionNo);

    /**
     * Создать новую dev-версию на основе указанной существующей версии.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param id        идентификатор продукта
     * @param versionNo номер версии-источника
     * @return новая dev-версия
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если уже есть другая версия в статусе dev
     */
    ProductVersionModel createVersionFrom(Long tenantId, Long id, Long versionNo);

    /**
     * Обновить dev-версию продукта (изменение JSON-модели).
     *
     * @param tenantId                 идентификатор тенанта ({@code tid})
     * @param id                       идентификатор продукта
     * @param versionNo                номер обновляемой версии
     * @param newProductVersionModel   новое описание
     * @return сохранённое описание версии
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если версия не в статусе dev
     */
    ProductVersionModel updateVersion(Long tenantId, Long id, Long versionNo, ProductVersionModel newProductVersionModel);

    /**
     * Мягкое удаление продукта (целиком).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id       идентификатор продукта
     */
    void softDeleteProduct(Long tenantId, Long id);

    /**
     * Удаление одной версии продукта (обычно только dev).
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param id        идентификатор продукта
     * @param versionNo номер версии
     * @throws ru.pt.api.dto.exception.NotFoundException если версия не найдена
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если удалять prod-версию нельзя по правилам домена
     */
    void deleteVersion(Long tenantId, Long id, Long versionNo);

    /**
     * Пример JSON для запроса предрасчёта (котировки) по версии продукта.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param id        идентификатор продукта
     * @param versionNo номер версии
     * @return строка JSON
     */
    String getJsonExampleQuote(Long tenantId, Long id, Long versionNo);

    /**
     * Пример JSON для запроса оформления / сохранения полиса по версии продукта.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param id        идентификатор продукта
     * @param versionNo номер версии
     * @return строка JSON
     */
    String getJsonExampleSave(Long tenantId, Long id, Long versionNo);

    /**
     * Актуальная версия продукта по идентификатору (dev или prod в зависимости от флага).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id       идентификатор продукта
     * @param forDev   {@code true} — приоритет dev-версии, {@code false} — prod
     * @return модель версии
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если нет подходящей версии
     */
    ProductVersionModel getProduct(Long tenantId, Long id, boolean forDev);

    /**
     * Версия продукта по коду продукта и номеру версии.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param code      код продукта
     * @param versionNo номер версии
     * @return модель версии
     */
    ProductVersionModel getProductByCodeAndVersionNo(Long tenantId, String code, Long versionNo);

    /**
     * Актуальная версия продукта по коду (dev или prod).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param code     код продукта
     * @param forDev   {@code true} — dev, {@code false} — prod
     * @return модель версии
     */
    ProductVersionModel getProductByCode(Long tenantId, String code, boolean forDev);

    /**
     * Продукты, доступные аккаунту (по правилам домена и привязок).
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param accountId идентификатор аккаунта
     * @return список сводок продуктов
     */
    List<Product> getProductByAccountId(Long tenantId, String accountId);

    /**
     * Все переменные из метаданных продукта ({@code pt_metadata} / связанная модель).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @return список {@link PvVar}
     */
    List<PvVar> getPvVars(Long tenantId);

    /**
     * Перезагрузить переменные из LOB для указанной категории в dev-версии продукта.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта
     * @param versionNo номер версии (должна быть dev)
     * @param category  категория переменных (например {@code policy}, {@code insuredObject})
     * @return обновлённая версия продукта
     * @throws ru.pt.api.dto.exception.BadRequestException если {@code category} пуст
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если версия не DEV или обновление недопустимо
     */
    ProductVersionModel reloadVars(Long tenantId, Long productId, Long versionNo, String category);
}

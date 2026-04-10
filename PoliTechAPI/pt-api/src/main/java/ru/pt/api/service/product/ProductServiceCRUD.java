package ru.pt.api.service.product;

import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.exception.UnprocessableEntityException;

public interface ProductServiceCRUD {

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
     * Актуальная версия продукта по идентификатору (dev или prod в зависимости от флага).
     *
     * @param tenantId идентификатор тенанта ({@code tid})
     * @param id       идентификатор продукта
     * @param forDev   {@code true} — приоритет dev-версии, {@code false} — prod
     * @return модель версии
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если нет подходящей версии
     */
    ProductVersionModel getProduct(Long tenantId, Long id, boolean forDev);

}

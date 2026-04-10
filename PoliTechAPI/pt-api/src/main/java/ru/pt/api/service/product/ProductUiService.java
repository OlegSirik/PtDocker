package ru.pt.api.service.product;

import ru.pt.api.dto.product.ProductFormData;

/**
 * Агрегированные данные для UI экранов продукта: примеры JSON, справочники по переменным, правила периодов.
 * <p>
 * Собирает информацию из {@link ProductService}, {@link ru.pt.db.service.RefDataService} и конфигурации версии продукта.
 * Параметр {@code tenantId} должен совпадать с тенантом текущего пользователя
 * ({@link ru.pt.api.security.AuthenticatedUser#getTenantId()}); реализация может дополнительно сверять контекст.
 * <p>
 * Авторизация: как минимум проверки, наследуемые от вызовов {@link ProductService} (ресурс
 * {@link ru.pt.api.service.auth.AuthZ.ResourceType#PRODUCT}).
 */
public interface ProductUiService {

    /**
     * Данные форм для продукта по его dev-версии: пример save, списки значений из {@code varList}/справочников,
     * вспомогательные структуры для валидации на клиенте.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта
     * @return сводный объект {@link ProductFormData}
     * @throws ru.pt.api.dto.exception.BadRequestException если контекст пользователя недоступен
     * @throws ru.pt.api.dto.exception.UnprocessableEntityException если нет подходящей dev-версии для форм
     */
    ProductFormData uiProductData(Long tenantId, Long productId);
}

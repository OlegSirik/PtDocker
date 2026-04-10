package ru.pt.api.service.product;

/**
 * Кэшированные примеры JSON для тестирования продукта (котировка и полис) по паре продукт + версия.
 * <p>
 * Данные хранятся в {@code pt_product_tests} (или аналоге): поля с примерами quote/policy.
 * Методы чтения с {@code tenantId} делегируют генерацию при отсутствии кэша в {@link ProductService};
 * {@code tenantId} должен совпадать с тенантом из контекста безопасности
 * ({@link ru.pt.api.security.AuthenticatedUser#getTenantId()}) для прохождения проверок вниз по стеку.
 */
public interface ProductTestService {

    /**
     * Получить пример JSON для котировки: из кэша или сгенерировать через {@link ProductService#getJsonExampleQuote}
     * и сохранить.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта ({@code pt_products.id})
     * @param versionNo номер версии
     * @return строка JSON
     */
    String getTestQuote(Long tenantId, Long productId, Long versionNo);

    /**
     * Получить пример JSON для полиса: из кэша или сгенерировать через {@link ProductService#getJsonExampleSave}
     * и сохранить.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта ({@code pt_products.id})
     * @param versionNo номер версии
     * @return строка JSON
     */
    String getTestPolicy(Long tenantId, Long productId, Long versionNo);

    /**
     * Сохранить (перезаписать) пример JSON котировки для пары продукт + версия.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта
     * @param versionNo номер версии
     * @param json      тело примера
     */
    void saveTestQuote(Long tenantId, Long productId, Long versionNo, String json);

    /**
     * Сохранить (перезаписать) пример JSON полиса для пары продукт + версия.
     *
     * @param tenantId  идентификатор тенанта ({@code tid})
     * @param productId идентификатор продукта
     * @param versionNo номер версии
     * @param json      тело примера
     */
    void saveTestPolicy(Long tenantId, Long productId, Long versionNo, String json);
}

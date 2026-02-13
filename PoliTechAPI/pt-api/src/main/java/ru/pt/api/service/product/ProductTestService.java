package ru.pt.api.service.product;

/**
 * Service for managing cached quote/policy JSON examples per product.
 */
public interface ProductTestService {

    /**
     * Get quote example JSON for product. Generates and caches if not found.
     *
     * @param productId product id (pt_products.id)
     * @param versionNo version number
     * @return JSON string
     */
    String getTestQuote(Integer productId, Integer versionNo);

    /**
     * Get policy example JSON for product. Generates and caches if not found.
     *
     * @param productId product id (pt_products.id)
     * @param versionNo version number
     * @return JSON string
     */
    String getTestPolicy(Integer productId, Integer versionNo);

    /**
     * Save quote example JSON for product.
     */
    void saveTestQuote(Integer productId, Integer versionNo, String json);

    /**
     * Save policy example JSON for product.
     */
    void saveTestPolicy(Integer productId, Integer versionNo, String json);
}

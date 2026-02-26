package ru.pt.api.service.marketplace;

import ru.pt.api.dto.marketplace.FormMetadata;
import ru.pt.api.dto.marketplace.MpOwnerResponse;
import ru.pt.api.dto.marketplace.MpProductDto;

import java.util.List;

/**
 * Service for marketplace integration endpoints.
 */
public interface MarketplaceService {

    /**
     * Get page owner info (legal data, logo, title).
     * TODO: Replace static data with database lookup.
     */
    MpOwnerResponse getPageOwner();

    /**
     * Get all products available for the current account (can_quote and can_policy).
     */
    List<MpProductDto> getAllProducts();

    /**
     * Get form metadata for a product by id.
     */
    FormMetadata getProduct(Integer productId);
}

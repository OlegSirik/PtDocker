package ru.pt.api.service.product;

import ru.pt.api.dto.product.PolicyFormDto;
import ru.pt.api.dto.product.ProductForQuoteDto;

import java.util.List;

/**
 * Global LK policy-form catalog ({@code pt_policy_forms} with {@code tid = 1}).
 * Not tenant-specific; same list for every tenant/product.
 */
public interface PolicyFormService {

    /** Catalog tenant id for hardcoded forms. */
    long CATALOG_TID = 1L;

    List<PolicyFormDto> listForms(Long tenantId);

    PolicyFormDto getByPageId(Long tenantId, String pageId);

    /** Create form in global catalog (always {@link #CATALOG_TID}). */
    PolicyFormDto create(Long tenantId, PolicyFormDto payload);

    /** Whether {@code pageId} exists in the global catalog. */
    boolean isKnownPageId(String pageId);

    /**
     * Products available for LK quote create.
     * Uses session {@code dataScope}: DEV → {@code dev_version_no} (fallback prod),
     * PROD → {@code prod_version_no}; version JSON must have {@code policyFormPageId}
     * present in the global catalog.
     */
    List<ProductForQuoteDto> listProductsForQuote(Long tenantId);
}

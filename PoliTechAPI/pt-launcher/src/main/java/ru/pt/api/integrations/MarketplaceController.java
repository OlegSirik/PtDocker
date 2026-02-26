package ru.pt.api.integrations;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.marketplace.FormMetadata;
import ru.pt.api.dto.marketplace.MpOwnerResponse;
import ru.pt.api.dto.marketplace.MpProductDto;
import ru.pt.api.service.marketplace.MarketplaceService;

import java.util.List;

import ru.pt.product.entity.ProductTestId;

/**
 * Marketplace integration API.
 * Requires X-Api-Key header for authentication.
 * URL Pattern: /api/v1/{tenantCode}/integrations/mp
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/integrations/mp")
@SecurityRequirement(name = "apiKeyAuth")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @GetMapping("/owner")
    @Operation(summary = "Get page owner", description = "Returns legal data, logo and title for marketplace page")
    public ResponseEntity<MpOwnerResponse> getOwner(
            @Parameter(description = "Tenant code") @PathVariable String tenantCode) {
        return ResponseEntity.ok(marketplaceService.getPageOwner());
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Returns products available for current account (can_quote and can_policy)")
    public ResponseEntity<List<MpProductDto>> getProducts(
            @Parameter(description = "Tenant code") @PathVariable String tenantCode) {
        return ResponseEntity.ok(marketplaceService.getAllProducts());
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get product form metadata", description = "Returns form metadata for a product")
    public ResponseEntity<FormMetadata> getProduct(
            @Parameter(description = "Tenant code") @PathVariable String tenantCode,
            @Parameter(description = "Product code") @PathVariable Integer productId) {
        return ResponseEntity.ok(marketplaceService.getProduct(productId));
    }
}

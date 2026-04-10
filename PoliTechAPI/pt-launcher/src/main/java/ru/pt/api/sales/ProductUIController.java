package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.product.ProductFormData;
import ru.pt.api.service.product.ProductUiService;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

/**
 * UI-данные по продукту (примеры JSON, списки).
 *
 * <p>URL: {@code /api/v1/{tenantCode}/admin/products/{productId}/...}
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/{tenantCode}/admin/products/{productId}")
@SecurityRequirement(name = "bearerAuth")
public class ProductUIController {

    private final ProductUiService productUiService;

    @GetMapping("/forms")
    public ProductFormData uiProductData(
            @PathVariable String tenantCode,
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetailsImpl user) {
                
        return productUiService.uiProductData(user.getTenantId(), productId);
    }
}

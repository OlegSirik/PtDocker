package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.product.ProductForQuoteDto;
import ru.pt.api.service.product.PolicyFormService;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

/**
 * LK: products available for quote creation (version by session dataScope + policyFormPageId).
 * URL: {@code /api/v1/{tenantCode}/lk/products-for-quote}
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/lk")
public class LkProductController {

    private final PolicyFormService policyFormService;

    @GetMapping("/products-for-quote")
    public ResponseEntity<List<ProductForQuoteDto>> productsForQuote(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(policyFormService.listProductsForQuote(user.getTenantId()));
    }
}

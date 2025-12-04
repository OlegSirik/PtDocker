package ru.pt.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.product.Product;
import ru.pt.api.service.product.ProductService;

import java.util.List;


/**
 * Контроллер для управления продуктами
 * <p>
 * URL Pattern: /api/v1/{tenantCode}/products
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/products")
@RequiredArgsConstructor
public class ProductController {

    public final ProductService productService;

    @GetMapping("/{accountId}")
    public List<Product> getProductsByAccountId(@PathVariable String tenantCode, @PathVariable String accountId) {
        return productService.getProductByAccountId(accountId);
    }
}

package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.product.Product;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления продуктами
 * Доступен только для SYS_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/products
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/admin/products")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SYS_ADMIN') or hasRole('TNT_ADMIN') or hasRole('PRODUCT_ADMIN')")
public class ProductController extends SecuredController {

    private final ProductService productService;

    public ProductController(ProductService productService, SecurityContextHelper securityContextHelper) {
        super(securityContextHelper);
        this.productService = productService;
    }

    @GetMapping
    public List<Product> list(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        //requireAdmin(user);
        return productService.listSummaries();
    }

    @PostMapping
    public ResponseEntity<ProductVersionModel> create(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ProductVersionModel payload) {
        //requireAdmin(user);
        ProductVersionModel created = productService.create(payload);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{accountId}")
    public List<Product> getProductsByAccountId(@PathVariable String tenantCode, @PathVariable String accountId) {
        return productService.getProductByAccountId(accountId);
    }

    @GetMapping("/{productId}/versions/{versionNo}")
    public ResponseEntity<ProductVersionModel> getVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo) {
        //requireAdmin(user);
        return ResponseEntity.ok(productService.getVersion(productId, versionNo));
    }

    @PostMapping("/{productId}/versions/{versionNo}/cmd/create")
    public ResponseEntity<ProductVersionModel> createVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo) {
        //requireRole(user, "SYS_ADMIN");
        return ResponseEntity.ok(productService.createVersionFrom(productId, versionNo));
    }

    @PostMapping("/{productId}/versions/{versionNo}/cmd/publish")
    public ResponseEntity<ProductVersionModel> publishVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo) {
        //requireRole(user, "SYS_ADMIN");
        return ResponseEntity.ok(productService.publishToProd(productId, versionNo));
    }

    @PutMapping("/{productId}/versions/{versionNo}")
    public ResponseEntity<ProductVersionModel> updateVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @RequestBody ProductVersionModel payload) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.updateVersion(productId, versionNo, payload));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId) {
        requireAdmin(user);
        productService.softDeleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/versions/{versionNo}")
    public ResponseEntity<Void> deleteVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        productService.deleteVersion(productId, versionNo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{productId}/versions/{versionNo}/example_quote")
    public ResponseEntity<String> getJsonExampleQuote(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.getJsonExampleQuote(productId, versionNo));
    }

    @GetMapping("/{productId}/versions/{versionNo}/example_save")
    public ResponseEntity<String> getJsonExampleSave(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.getJsonExampleSave(productId, versionNo));
    }
}



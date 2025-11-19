package ru.pt.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController extends SecuredController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Map<String, Object>> list(@AuthenticationPrincipal UserDetailsImpl user) {
        requireAdmin(user);
        return productService.listSummaries();
    }

    @PostMapping
    public ResponseEntity<ProductVersionModel> create(
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ProductVersionModel payload) {
        requireAdmin(user);
        ProductVersionModel created = productService.create(payload);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}/versions/{versionNo}")
    public ResponseEntity<ProductVersionModel> getVersion(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.getVersion(id, versionNo));
    }

    @PostMapping("/{id}/versions/{versionNo}/cmd/create")
    public ResponseEntity<ProductVersionModel> createVersion(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.createVersionFrom(id, versionNo));
    }

    @PutMapping("/{id}/versions/{versionNo}")
    public ResponseEntity<ProductVersionModel> updateVersion(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @PathVariable("versionNo") Integer versionNo,
            @RequestBody ProductVersionModel payload) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.updateVersion(id, versionNo, payload));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id) {
        requireAdmin(user);
        productService.softDeleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/versions/{versionNo}")
    public ResponseEntity<Void> deleteVersion(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        productService.deleteVersion(id, versionNo);
        return ResponseEntity.noContent().build();
    }

    // get /admin/lobs/example returns json example
    @GetMapping("/{id}/versions/{versionNo}/example_quote")
    public ResponseEntity<String> getJsonExampleQuote(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.getJsonExampleQuote(id, versionNo));
    }

    // get /admin/lobs/example returns json example
    @GetMapping("/{id}/versions/{versionNo}/example_save")
    public ResponseEntity<String> getJsonExampleSave(
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("id") Integer id,
            @PathVariable("versionNo") Integer versionNo) {
        requireAdmin(user);
        return ResponseEntity.ok(productService.getJsonExampleSave(id, versionNo));
    }
}



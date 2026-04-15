package ru.pt.api.sales;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.product.Product;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvCover;
import ru.pt.api.dto.product.PvPackage;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

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
//@PreAuthorize("hasRole('SYS_ADMIN') or hasRole('TNT_ADMIN') or hasRole('PRODUCT_ADMIN')")
public class ProductController extends SecuredController {

    private final ProductService productService;

    public ProductController(ProductService productService, SecurityContextHelper securityContextHelper) {
        super(securityContextHelper);
        this.productService = productService;
    }

    /**
     * Список продуктов. Фильтр по страховой: {@code GET .../products?insComp={insComp}}.
     */
    @GetMapping
    public List<Product> list(
            @PathVariable String tenantCode,
            @RequestParam(required = false) Long insComp,
            @AuthenticationPrincipal UserDetailsImpl user) {
        //requireAdmin(user);
        return productService.listSummaries(user.getTenantId(), insComp);
    }

    @PostMapping
    public ResponseEntity<ProductVersionModel> create(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody ProductVersionModel payload) {
        //requireAdmin(user);
        
        ProductVersionModel created = productService.create(user.getTenantId(), payload);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{accountId}")
    public List<Product> getProductsByAccountId(
            @PathVariable String tenantCode,
            @PathVariable String accountId,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return productService.getProductByAccountId(user.getTenantId(), accountId);
    }

    @GetMapping("/{productId}/versions/{versionNo}")
    public ResponseEntity<ProductVersionModel> getVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo) {
        //requireAdmin(user);
        return ResponseEntity.ok(productService.getVersion(user.getTenantId(), productId, versionNo));
    }

    @PostMapping("/{productId}/versions/{versionNo}/cmd/create")
    public ResponseEntity<ProductVersionModel> createVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo) {
        //requireRole(user, "SYS_ADMIN");
        return ResponseEntity.ok(productService.createVersionFrom(user.getTenantId(), productId, versionNo));
    }

    @PostMapping("/{productId}/versions/{versionNo}/cmd/publish")
    public ResponseEntity<ProductVersionModel> publishVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo) {
        //requireRole(user, "SYS_ADMIN");
        return ResponseEntity.ok(productService.publishToProd(user.getTenantId(), productId, versionNo));
    }

    @PostMapping("/{productId}/versions/{versionNo}/cmd/reload")
    public ResponseEntity<ProductVersionModel> reloadVars(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestParam("cat") String category) {
        //requireRole(user, "SYS_ADMIN");
        return ResponseEntity.ok(productService.reloadVars(user.getTenantId(), productId, versionNo, category));
    }

    @PutMapping("/{productId}/versions/{versionNo}")
    public ResponseEntity<ProductVersionModel> updateVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestBody ProductVersionModel payload) {
        //requireAdmin(user);
        return ResponseEntity.ok(productService.updateVersion(user.getTenantId(), productId, versionNo, payload));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId) {
        //requireAdmin(user);
        productService.softDeleteProduct(user.getTenantId(), productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/versions/{versionNo}")
    public ResponseEntity<Void> deleteVersion(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo) {
        //requireAdmin(user);
        productService.deleteVersion(user.getTenantId(), productId, versionNo);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{productId}/versions/{versionNo}/packages")
    public ResponseEntity<ProductVersionModel> addPackage(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestBody PvPackage payload) {
        return ResponseEntity.ok(productService.addPackage(user.getTenantId(), productId, versionNo, payload));
    }

    @PutMapping("/{productId}/versions/{versionNo}/packages/{packageCode}")
    public ResponseEntity<ProductVersionModel> updatePackage(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageCode") String packageCode,
            @RequestBody PvPackage payload) {
        payload.setCode(packageCode);
        return ResponseEntity.ok(productService.updatePackage(user.getTenantId(), productId, versionNo, payload));
    }

    @DeleteMapping("/{productId}/versions/{versionNo}/packages/{packageCode}")
    public ResponseEntity<ProductVersionModel> deletePackage(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageCode") String packageCode) {
        return ResponseEntity.ok(productService.deletePackage(user.getTenantId(), productId, versionNo, packageCode));
    }

    @PostMapping("/{productId}/versions/{versionNo}/packages/{packageCode}/covers")
    public ResponseEntity<ProductVersionModel> addCover(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageCode") String packageCode,
            @RequestBody PvCover payload) {
        return ResponseEntity.ok(productService.addCover(user.getTenantId(), productId, versionNo, packageCode, payload));
    }

    @PutMapping("/{productId}/versions/{versionNo}/packages/{packageCode}/covers/{coverCode}")
    public ResponseEntity<ProductVersionModel> updateCover(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageCode") String packageCode,
            @PathVariable("coverCode") String coverCode,
            @RequestBody PvCover payload) {
        payload.setCode(coverCode);
        return ResponseEntity.ok(productService.updateCover(user.getTenantId(), productId, versionNo, packageCode, payload));
    }

    @DeleteMapping("/{productId}/versions/{versionNo}/packages/{packageCode}/covers/{coverCode}")
    public ResponseEntity<ProductVersionModel> deleteCover(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("packageCode") String packageCode,
            @PathVariable("coverCode") String coverCode) {
        return ResponseEntity.ok(productService.deleteCover(user.getTenantId(), productId, versionNo, packageCode, coverCode));
    }

    @PostMapping("/{productId}/versions/{versionNo}/vars")
    public ResponseEntity<ProductVersionModel> addVar(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestBody PvVar payload) {
        return ResponseEntity.ok(productService.addVar(user.getTenantId(), productId, versionNo, payload));
    }

    @PutMapping("/{productId}/versions/{versionNo}/vars/{varCode}")
    public ResponseEntity<ProductVersionModel> updateVar(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("varCode") String varCode,
            @RequestBody PvVar payload) {
        payload.setVarCode(varCode);
        return ResponseEntity.ok(productService.updateVar(user.getTenantId(), productId, versionNo, payload));
    }

    @DeleteMapping("/{productId}/versions/{versionNo}/vars/{varCode}")
    public ResponseEntity<ProductVersionModel> deleteVar(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @PathVariable("varCode") String varCode) {
        return ResponseEntity.ok(productService.deleteVar(user.getTenantId(), productId, versionNo, varCode));
    }

    @PostMapping("/{productId}/versions/{versionNo}/validators")
    public ResponseEntity<ProductVersionModel> addValidator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestBody ValidatorRule payload) {
        return ResponseEntity.ok(productService.addValidator(user.getTenantId(), productId, versionNo, payload));
    }

    @PutMapping("/{productId}/versions/{versionNo}/validators")
    public ResponseEntity<ProductVersionModel> updateValidator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestBody ValidatorRule payload) {
        return ResponseEntity.ok(productService.updateValidator(user.getTenantId(), productId, versionNo, payload));
    }

    @DeleteMapping("/{productId}/versions/{versionNo}/validators")
    public ResponseEntity<ProductVersionModel> deleteValidator(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo,
            @RequestBody ValidatorRule payload) {
        return ResponseEntity.ok(productService.deleteValidator(user.getTenantId(), productId, versionNo, payload));
    }

    @GetMapping(value = "/{productId}/versions/{versionNo}/example_quote", produces = "application/json")
    public ResponseEntity<String> getJsonExampleQuote(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo) {
        //requireAdmin(user);
        return ResponseEntity.ok(productService.getJsonExampleQuote(user.getTenantId(), productId, versionNo));
    }

    @GetMapping(value = "/{productId}/versions/{versionNo}/example_save", produces = "application/json")
    public ResponseEntity<String> getJsonExampleSave(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @PathVariable("productId") Long productId,
            @PathVariable("versionNo") Long versionNo) {
        //requireAdmin(user);
        return ResponseEntity.ok(productService.getJsonExampleSave(user.getTenantId(), productId, versionNo));
    }

    /**
     * Get all PvVars from pt_metadata table
     */
    @GetMapping("/vars")
    public ResponseEntity<List<PvVar>> getPvVars(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(productService.getPvVars(user.getTenantId()));
    }
}



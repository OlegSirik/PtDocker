package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.service.product.ProductTestService;
import ru.pt.auth.security.UserDetailsImpl;

@RestController
@RequestMapping("/api/v1/{tenantCode}/admin/text")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProductTestController {

    private final ProductTestService productTestService;

    @GetMapping("/quote/{productId}/{versionNo}")
    public ResponseEntity<String> getTestQuote(
            @PathVariable String tenantCode,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(productTestService.getTestQuote(productId, versionNo));
    }

    @GetMapping("/policy/{productId}/{versionNo}")
    public ResponseEntity<String> getTestPolicy(
            @PathVariable String tenantCode,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(productTestService.getTestPolicy(productId, versionNo));
    }

    @PostMapping("/quote/{productId}/{versionNo}")
    public ResponseEntity<Void> saveTestQuote(
            @PathVariable String tenantCode,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @RequestBody String json,
            @AuthenticationPrincipal UserDetailsImpl user) {
        productTestService.saveTestQuote(productId, versionNo, json);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/policy/{productId}/{versionNo}")
    public ResponseEntity<Void> saveTestPolicy(
            @PathVariable String tenantCode,
            @PathVariable("productId") Integer productId,
            @PathVariable("versionNo") Integer versionNo,
            @RequestBody String json,
            @AuthenticationPrincipal UserDetailsImpl user) {
        productTestService.saveTestPolicy(productId, versionNo, json);
        return ResponseEntity.noContent().build();
    }
}

package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.product.LobVar;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.UserDetailsImpl;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing contract schema (sections, entities, attributes)
 * URL Pattern: /api/v1/{tenantCode}/admin/schema/{contract_code}
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/schema/{contractCode}/attributes")
public class SchemaController  {

    private final SchemaService schemaService;

    /**
     * GET /admin/schema/{contract_code}/attributes
     * Return Tree of sections, entities and attributes for current tid and contract code
     */
    @GetMapping
    public ResponseEntity<List<LobVar>> getAttributes(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        List<LobVar> attributes = schemaService.getAttributes(user.getTenantId(), contractCode);
        return ResponseEntity.ok(attributes);
    }

    /**
     * GET .../attributes/metadata — вложенный JSON по дереву атрибутов (ключ {@code code}, листья {@code ""}).
     */
    @GetMapping(value = "/metadata", produces = "application/json")
    public ResponseEntity<String> getAttributesMetadataJson(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(schemaService.getAttributesMetadataJson(user.getTenantId(), contractCode));
    }

    /**
     * POST .../attributes/metadata/with-values — JSON по схеме с фильтром (системные + ключи из тела)
     * и подстановкой значений из тела запроса ({@code var_code} → значение).
     */
    @PostMapping(value = "/metadata/with-values", produces = "application/json")
    public ResponseEntity<String> getAttributesMetadataJsonWithValues(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody(required = false) Map<String, String> varValues,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(schemaService.getAttributesMetadataJson(user.getTenantId(), contractCode, varValues));
    }

   @PostMapping
   public ResponseEntity<LobVar> addAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody LobVar lobVar,
            @AuthenticationPrincipal UserDetailsImpl user) {
        schemaService.addAttribute(user.getTenantId(), contractCode, lobVar);
        return ResponseEntity.status(HttpStatus.CREATED).body(lobVar);
    }

    @PutMapping
    public ResponseEntity<LobVar> updateAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody LobVar lobVar,
            @AuthenticationPrincipal UserDetailsImpl user) {
        schemaService.updateAttribute(user.getTenantId(), contractCode, lobVar);
        return ResponseEntity.ok(lobVar);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody LobVar lobVar,
            @AuthenticationPrincipal UserDetailsImpl user) {
        schemaService.deleteAttribute(user.getTenantId(), contractCode, lobVar);
        return ResponseEntity.noContent().build();
    }
}

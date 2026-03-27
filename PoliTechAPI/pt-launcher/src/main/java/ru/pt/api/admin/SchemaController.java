package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.product.LobVar;
import ru.pt.api.service.schema.SchemaService;

import java.util.List;

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
            @PathVariable String contractCode) {
        List<LobVar> attributes = schemaService.getAttributes(tenantCode, contractCode);
        return ResponseEntity.ok(attributes);
    }

   @PostMapping
   public ResponseEntity<LobVar> addAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody LobVar lobVar) {
        schemaService.addAttribute(tenantCode, contractCode, lobVar);
        return ResponseEntity.status(HttpStatus.CREATED).body(lobVar);
    }

    @PutMapping
    public ResponseEntity<LobVar> updateAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody LobVar lobVar) {
        schemaService.updateAttribute(tenantCode, contractCode, lobVar);
        return ResponseEntity.ok(lobVar);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @RequestBody LobVar lobVar) {
        schemaService.deleteAttribute(tenantCode, contractCode, lobVar);
        return ResponseEntity.noContent().build();
    }
}

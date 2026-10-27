package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.schema.ContractSchemaExportDto;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.UserDetailsImpl;

/**
 * Экспорт/импорт модели договора (JSON).
 * <ul>
 *   <li>GET {@code /api/v1/{tenant}/admin/schema/{contractCode}/export}</li>
 *   <li>POST {@code /api/v1/{tenant}/admin/schema/import}</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/schema")
public class ContractSchemaExportController {

    private final SchemaService schemaService;

    @GetMapping("/{contractCode}/export")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<ContractSchemaExportDto> exportSchema(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(schemaService.exportContractSchema(user.getTenantId(), contractCode));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<ContractSchemaExportDto> importSchema(
            @PathVariable String tenantCode,
            @RequestBody ContractSchemaExportDto body,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(schemaService.importContractSchema(user.getTenantId(), body));
    }
}

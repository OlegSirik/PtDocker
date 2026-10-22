package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.product.ContractModelDto;
import ru.pt.api.service.schema.SchemaService;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

/**
 * Список моделей договора ({@code mt_contract_model}) для админки схемы.
 * URL: {@code /api/v1/{tenantCode}/admin/schema/models}
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/schema/models")
public class ContractModelController {

    private final SchemaService schemaService;

    @GetMapping
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<List<ContractModelDto>> listModels(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(schemaService.listContractModels(user.getTenantId()));
    }
}

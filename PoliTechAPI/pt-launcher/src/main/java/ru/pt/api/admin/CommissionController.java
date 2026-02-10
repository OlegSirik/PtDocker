package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.commission.CommissionAction;
import ru.pt.api.dto.commission.CommissionRateDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.commission.CommissionService;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;

/**
 * Controller for commission rate configurations.
 * URL Pattern: /api/v1/{tenantCode}/admin/commissions
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/commissions")
public class CommissionController extends SecuredController {

    private final CommissionService commissionService;

    public CommissionController(SecurityContextHelper securityContextHelper,
                               CommissionService commissionService) {
        super(securityContextHelper);
        this.commissionService = commissionService;
    }

    /**
     * GET /configurations with optional query parameters productId, accountId, action.
     * If no parameters passed, returns empty list.
     */
    @GetMapping("/configurations")
    public ResponseEntity<List<CommissionRateDto>> getConfigurations(
            @PathVariable String tenantCode,
            @RequestParam(required = true) Long accountId,
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String action) {
        List<CommissionRateDto> list = commissionService.getConfigurations(accountId, productId, CommissionAction.fromValue(action));
        return ResponseEntity.ok(list);
    }

    /**
     * POST /configurations - create new commission configuration
     */
    @PostMapping("/configurations")
    public ResponseEntity<CommissionRateDto> createConfiguration(
            @PathVariable String tenantCode,
            @RequestBody CommissionRateDto dto) {
        CommissionRateDto created = commissionService.createProductCommission(dto);
        return ResponseEntity.ok(created);
    }

    /**
     * PUT /configurations/{id} - update commission configuration
     */
    @PutMapping("/configurations/{id}")
    public ResponseEntity<CommissionRateDto> updateConfiguration(
            @PathVariable String tenantCode,
            @PathVariable Long id,
            @RequestBody CommissionRateDto dto) {
        dto.setId(id);
        CommissionRateDto updated = commissionService.updateProductCommission(dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /configurations/{id} - soft delete commission configuration
     */
    @DeleteMapping("/configurations/{id}")
    public ResponseEntity<Void> deleteConfiguration(
            @PathVariable String tenantCode,
            @PathVariable Long id) {
        commissionService.deleteProductCommission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /configurations/{id} - get commission configuration by id
     */
    @GetMapping("/configurations/{id}")
    public ResponseEntity<CommissionRateDto> getConfigurationById(
            @PathVariable String tenantCode,
            @PathVariable Long id) {
        CommissionRateDto dto = commissionService.getConfigurationById(id);
        return ResponseEntity.ok(dto);
    }
}

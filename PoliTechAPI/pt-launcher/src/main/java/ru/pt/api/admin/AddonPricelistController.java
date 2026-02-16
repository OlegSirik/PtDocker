package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.addon.PricelistDto;
import ru.pt.api.dto.addon.PricelistListDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.addon.AddOnPricelistService;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;

/**
 * Controller for add-on pricelists.
 * URL Pattern: /api/v1/{tenantCode}/admin/addon/pricelists
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/addon/pricelists")
public class AddonPricelistController extends SecuredController {

    private final AddOnPricelistService addOnPricelistService;

    public AddonPricelistController(SecurityContextHelper securityContextHelper,
                                    AddOnPricelistService addOnPricelistService) {
        super(securityContextHelper);
        this.addOnPricelistService = addOnPricelistService;
    }

    @GetMapping
    public ResponseEntity<List<PricelistListDto>> getPricelists(
            @PathVariable String tenantCode,
            @RequestParam(required = false) Long spId) {
        return ResponseEntity.ok(addOnPricelistService.getPricelists(spId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PricelistDto> getPricelist(@PathVariable String tenantCode, @PathVariable Long id) {
        return ResponseEntity.ok(addOnPricelistService.getPricelist(id));
    }

    @PostMapping
    public ResponseEntity<PricelistDto> createPricelist(@PathVariable String tenantCode, @RequestBody PricelistDto dto) {
        return ResponseEntity.ok(addOnPricelistService.createPricelist(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PricelistDto> updatePricelist(@PathVariable String tenantCode, @PathVariable Long id, @RequestBody PricelistDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(addOnPricelistService.updatePricelist(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePricelist(@PathVariable String tenantCode, @PathVariable Long id) {
        addOnPricelistService.deletePricelist(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspendPricelist(@PathVariable String tenantCode, @PathVariable Long id) {
        addOnPricelistService.suspendPricelist(id);
        return ResponseEntity.noContent().build();
    }
}

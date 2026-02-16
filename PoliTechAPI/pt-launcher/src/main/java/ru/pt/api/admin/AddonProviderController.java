package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.addon.ProviderDto;
import ru.pt.api.dto.addon.ProviderListDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.addon.AddOnProviderService;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;

/**
 * Controller for add-on providers.
 * URL Pattern: /api/v1/{tenantCode}/admin/addon/providers
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/addon/providers")
public class AddonProviderController extends SecuredController {

    private final AddOnProviderService addOnProviderService;

    public AddonProviderController(SecurityContextHelper securityContextHelper,
                                   AddOnProviderService addOnProviderService) {
        super(securityContextHelper);
        this.addOnProviderService = addOnProviderService;
    }

    @GetMapping
    public ResponseEntity<List<ProviderListDto>> getProviders(@PathVariable String tenantCode) {
        return ResponseEntity.ok(addOnProviderService.getProviders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProviderDto> getProvider(@PathVariable String tenantCode, @PathVariable Long id) {
        return ResponseEntity.ok(addOnProviderService.getProvider(id));
    }

    @PostMapping
    public ResponseEntity<ProviderDto> createProvider(@PathVariable String tenantCode, @RequestBody ProviderDto dto) {
        return ResponseEntity.ok(addOnProviderService.createProvider(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProviderDto> updateProvider(@PathVariable String tenantCode, @PathVariable Long id, @RequestBody ProviderDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(addOnProviderService.updateProvider(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> suspendProvider(@PathVariable String tenantCode, @PathVariable Long id) {
        addOnProviderService.suspendProvider(id);
        return ResponseEntity.noContent().build();
    }
}

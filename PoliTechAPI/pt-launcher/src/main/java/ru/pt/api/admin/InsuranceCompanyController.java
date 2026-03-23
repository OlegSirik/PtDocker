package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.product.InsuranceCompanyDto;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.product.InsCompanyService;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.List;

/**
 * Страховые компании. URL: /api/v1/{tenantCode}/admin/insCompanies
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/insCompanies")
public class InsuranceCompanyController extends SecuredController {

    private final InsCompanyService insCompanyService;

    public InsuranceCompanyController(SecurityContextHelper securityContextHelper,
                                      InsCompanyService insCompanyService) {
        super(securityContextHelper);
        this.insCompanyService = insCompanyService;
    }

    @GetMapping
    public ResponseEntity<List<InsuranceCompanyDto>> list(@PathVariable String tenantCode) {
        return ResponseEntity.ok(insCompanyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceCompanyDto> get(@PathVariable String tenantCode, @PathVariable Long id) {
        return ResponseEntity.ok(insCompanyService.get(id));
    }

    @PostMapping
    public ResponseEntity<InsuranceCompanyDto> create(@PathVariable String tenantCode, @RequestBody InsuranceCompanyDto dto) {
        return ResponseEntity.ok(insCompanyService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsuranceCompanyDto> update(
            @PathVariable String tenantCode,
            @PathVariable Long id,
            @RequestBody InsuranceCompanyDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(insCompanyService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String tenantCode, @PathVariable Long id) {
        insCompanyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

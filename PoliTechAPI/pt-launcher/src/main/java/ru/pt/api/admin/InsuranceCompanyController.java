package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.product.InsuranceCompanyDto;
import ru.pt.api.service.product.InsCompanyService;
import ru.pt.auth.security.UserDetailsImpl;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

/**
 * Страховые компании. URL: /api/v1/{tenantCode}/admin/insCompanies
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/insCompanies")
public class InsuranceCompanyController  {

    private final InsCompanyService insCompanyService;

    public InsuranceCompanyController(InsCompanyService insCompanyService) {
        this.insCompanyService = insCompanyService;
    }

    @GetMapping
    public ResponseEntity<List<InsuranceCompanyDto>> list(
        @PathVariable String tenantCode,
        @AuthenticationPrincipal UserDetailsImpl user) {
        
        return ResponseEntity.ok(insCompanyService.getAll(user.getTenantId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsuranceCompanyDto> get(
        @PathVariable String tenantCode, 
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(insCompanyService.get(user.getTenantId(), id));
    }

    @PostMapping
    public ResponseEntity<InsuranceCompanyDto> create(
        @PathVariable String tenantCode, 
        @RequestBody InsuranceCompanyDto dto,
        @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(insCompanyService.create(user.getTenantId(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsuranceCompanyDto> update(
            @PathVariable String tenantCode,
            @PathVariable Long id,
            @RequestBody InsuranceCompanyDto dto,
            @AuthenticationPrincipal UserDetailsImpl user) {
        dto.setId(id);
        return ResponseEntity.ok(insCompanyService.update(user.getTenantId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable String tenantCode, 
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetailsImpl user) {
        insCompanyService.delete(user.getTenantId(), id);
        return ResponseEntity.noContent().build();
    }
}

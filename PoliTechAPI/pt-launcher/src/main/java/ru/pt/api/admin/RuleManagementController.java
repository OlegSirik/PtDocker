package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.rules.RuleDto;
import ru.pt.api.dto.rules.RuleScopeType;
import ru.pt.api.dto.rules.RuleType;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.rules.RuleManagementService;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/rules")
public class RuleManagementController extends SecuredController {

    private final RuleManagementService ruleManagementService;

    public RuleManagementController(
            SecurityContextHelper securityContextHelper,
            RuleManagementService ruleManagementService) {
        super(securityContextHelper);
        this.ruleManagementService = ruleManagementService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<List<RuleDto>> list(
            @RequestParam(required = false) RuleType ruleType,
            @RequestParam(required = false) RuleScopeType scopeType,
            @RequestParam(required = false) String scopeCode,
            @RequestParam(required = false, defaultValue = "ACTIVE") String recordStatus) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(ruleManagementService.list(
                user.getTenantId(), ruleType, scopeType, scopeCode, recordStatus));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RuleDto> get(@PathVariable Long id) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(ruleManagementService.getById(user.getTenantId(), id));
    }

    @PostMapping
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RuleDto> create(@RequestBody RuleDto dto) {
        UserDetailsImpl user = getCurrentUser();
        RuleDto created = ruleManagementService.create(user.getTenantId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<RuleDto> update(@PathVariable Long id, @RequestBody RuleDto dto) {
        UserDetailsImpl user = getCurrentUser();
        return ResponseEntity.ok(ruleManagementService.update(user.getTenantId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        UserDetailsImpl user = getCurrentUser();
        ruleManagementService.delete(user.getTenantId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cmd/reload")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, String>> reload() {
        ruleManagementService.reloadCache();
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}

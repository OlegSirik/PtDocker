package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.llm.TenantLlmApiKeyUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigView;
import ru.pt.api.dto.llm.TenantLlmRuntimeConfig;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.llm.TenantLlmConfigService;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.TenantService;
import ru.pt.product.llm.provider.LlmCompletionRequest;
import ru.pt.product.llm.provider.LlmCompletionResult;
import ru.pt.product.llm.provider.LlmGateway;
import ru.pt.product.llm.provider.LlmMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/tenant/llm-config")
public class TenantLlmConfigController extends SecuredController {

    private final TenantLlmConfigService tenantLlmConfigService;
    private final TenantService tenantService;
    private final LlmGateway llmGateway;

    public TenantLlmConfigController(
            SecurityContextHelper securityContextHelper,
            TenantLlmConfigService tenantLlmConfigService,
            TenantService tenantService,
            LlmGateway llmGateway) {
        super(securityContextHelper);
        this.tenantLlmConfigService = tenantLlmConfigService;
        this.tenantService = tenantService;
        this.llmGateway = llmGateway;
    }

    @GetMapping
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<TenantLlmConfigView> getConfig(@PathVariable String tenantCode) {
        Long tenantId = resolveTargetTenantId(tenantCode);
        return ResponseEntity.ok(tenantLlmConfigService.getConfigView(tenantId));
    }

    @PutMapping
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<TenantLlmConfigView> saveConfig(
            @PathVariable String tenantCode,
            @RequestBody TenantLlmConfigUpdateRequest request) {
        Long tenantId = resolveTargetTenantId(tenantCode);
        return ResponseEntity.ok(tenantLlmConfigService.saveConfig(tenantId, request));
    }

    @PutMapping("/api-key")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<TenantLlmConfigView> updateApiKey(
            @PathVariable String tenantCode,
            @RequestBody TenantLlmApiKeyUpdateRequest request) {
        Long tenantId = resolveTargetTenantId(tenantCode);
        return ResponseEntity.ok(tenantLlmConfigService.updateApiKey(tenantId, request));
    }

    @PostMapping("/test")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable String tenantCode) {
        Long tenantId = resolveTargetTenantId(tenantCode);
        TenantLlmRuntimeConfig config = tenantLlmConfigService.resolve(tenantId);
        LlmCompletionResult result = llmGateway.complete(
                new LlmCompletionRequest(
                        List.of(new LlmMessage("user", "Reply with OK")),
                        null,
                        0.0,
                        16,
                        false),
                config,
                null,
                null);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("provider", config.getDefaultProvider());
        body.put("model", result.model());
        body.put("latencyMs", result.latencyMs());
        return ResponseEntity.ok(body);
    }

    private Long resolveTargetTenantId(String tenantCode) {
        TenantEntity tenant = tenantService.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantCode));
        UserDetailsImpl user = getCurrentUser();
        if (!"SYS_ADMIN".equals(user.getUserRole())
                && !tenant.getId().equals(user.getTenantId())) {
            throw new ForbiddenException("Access denied to tenant LLM config");
        }
        return tenant.getId();
    }
}

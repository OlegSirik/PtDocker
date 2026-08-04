package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.llm.TenantLlmApiKeyUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigView;
import ru.pt.api.dto.llm.TenantLlmRuntimeConfig;
import ru.pt.api.service.llm.TenantLlmConfigService;
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
public class TenantLlmConfigController {

    private final TenantLlmConfigService tenantLlmConfigService;
    private final LlmGateway llmGateway;

    public TenantLlmConfigController(
            TenantLlmConfigService tenantLlmConfigService,
            LlmGateway llmGateway) {
        this.tenantLlmConfigService = tenantLlmConfigService;
        this.llmGateway = llmGateway;
    }

    @GetMapping
    public ResponseEntity<TenantLlmConfigView> getConfig(@PathVariable String tenantCode) {
        return ResponseEntity.ok(tenantLlmConfigService.getConfigView(tenantCode));
    }

    @PutMapping
    public ResponseEntity<TenantLlmConfigView> saveConfig(
            @PathVariable String tenantCode,
            @RequestBody TenantLlmConfigUpdateRequest request) {
        return ResponseEntity.ok(tenantLlmConfigService.saveConfig(tenantCode, request));
    }

    @PutMapping("/api-key")
    public ResponseEntity<TenantLlmConfigView> updateApiKey(
            @PathVariable String tenantCode,
            @RequestBody TenantLlmApiKeyUpdateRequest request) {
        return ResponseEntity.ok(tenantLlmConfigService.updateApiKey(tenantCode, request));
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection(@PathVariable String tenantCode) {
        TenantLlmRuntimeConfig config = tenantLlmConfigService.resolveForTest(tenantCode);
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
}

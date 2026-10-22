package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.llm.LlmAssistResponse;
import ru.pt.api.dto.llm.LlmSchemaAssistRequest;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.service.product.LlmAssistantService;
import ru.pt.auth.security.UserDetailsImpl;

/**
 * LLM assist for contract schema attributes.
 * URL: {@code POST /api/v1/{tenantCode}/admin/schema/{contractCode}/llm/assist}
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/schema/{contractCode}/llm")
public class SchemaLlmController {

    private final LlmAssistantService llmAssistantService;

    @PostMapping("/assist")
    @PreAuthorize("hasRole('PRODUCT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<LlmAssistResponse> llmAssist(
            @PathVariable String tenantCode,
            @PathVariable String contractCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody LlmSchemaAssistRequest request) {
        if (request == null) {
            request = new LlmSchemaAssistRequest();
        }
        request.setContractCode(contractCode);
        request.setTaskType(LlmTaskType.SCHEMA);
        return ResponseEntity.ok(llmAssistantService.assistSchema(request, user));
    }
}

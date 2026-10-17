package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pt.api.dto.product.PolicyFormDto;
import ru.pt.api.service.product.PolicyFormService;
import ru.pt.auth.security.UserDetailsImpl;

import java.util.List;

/**
 * Global catalog of LK policy-entry forms ({@code pt_policy_forms}, {@code tid = 1}).
 * URL: {@code /api/v1/{tenantCode}/admin/policy-forms}
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/policy-forms")
public class PolicyFormController {

    private final PolicyFormService policyFormService;

    @GetMapping
    public ResponseEntity<List<PolicyFormDto>> list(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(policyFormService.listForms(user.getTenantId()));
    }

    /** Same full catalog as {@link #list} — forms are not filtered by LOB. */
    @GetMapping("/by-lob/{lobCode}")
    public ResponseEntity<List<PolicyFormDto>> listByLob(
            @PathVariable String tenantCode,
            @PathVariable String lobCode,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(policyFormService.listForms(user.getTenantId()));
    }

    @GetMapping("/{pageId}")
    public ResponseEntity<PolicyFormDto> getByPageId(
            @PathVariable String tenantCode,
            @PathVariable String pageId,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(policyFormService.getByPageId(user.getTenantId(), pageId));
    }

    @PostMapping
    public ResponseEntity<PolicyFormDto> create(
            @PathVariable String tenantCode,
            @AuthenticationPrincipal UserDetailsImpl user,
            @RequestBody PolicyFormDto payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(policyFormService.create(user.getTenantId(), payload));
    }
}

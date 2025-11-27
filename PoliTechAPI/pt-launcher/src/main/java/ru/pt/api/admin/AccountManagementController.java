package ru.pt.api.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления аккаунтами
 * Доступен для GROUP_ADMIN и PRODUCT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/accounts
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequestMapping("/api/v1/{tenantCode}/admin/accounts")
public class AccountManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;

    public AccountManagementController(SecurityContextHelper securityContextHelper,
                                      AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.adminUserManagementService = adminUserManagementService;
    }

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Создание аккаунта
     * POST /api/v1/{tenantCode}/admin/accounts
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createAccount(
            @RequestBody CreateAccountRequest request) {
        try {
            requireAnyRole("GROUP_ADMIN", "PRODUCT_ADMIN");

            Map<String, Object> result = adminUserManagementService.createAccount(
                    request.getParentAccountId(),
                    request.getAccountName(),
                    request.getNodeType()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", result.get("id"));
            response.put("name", result.get("name"));
            response.put("nodeType", result.get("nodeType"));
            response.put("parentId", result.get("parentId"));

            return buildCreatedResponse(response, "Account created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Получить иерархию аккаунтов
     * GET /api/v1/{tenantId}/admin/accounts/hierarchy
     */
    @GetMapping("/hierarchy")
    @PreAuthorize("hasAnyRole('GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAccountsHierarchy(@PathVariable String tenantId) {
        try {
            requireAnyRole("GROUP_ADMIN", "PRODUCT_ADMIN");

            List<Map<String, Object>> hierarchy = adminUserManagementService.getAccountsHierarchy();
            return ResponseEntity.ok(hierarchy);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // DTO Classes
    public static class CreateAccountRequest {
        private Long parentAccountId;
        private String accountName;
        private String nodeType;

        public Long getParentAccountId() {
            return parentAccountId;
        }

        public void setParentAccountId(Long parentAccountId) {
            this.parentAccountId = parentAccountId;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }
    }
}


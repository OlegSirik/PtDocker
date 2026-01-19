package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AccountServiceImpl;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Контроллер для управления аккаунтами
 * Доступен для GROUP_ADMIN и PRODUCT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/accounts
 * tenantCode: pt, vsk, msg
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/accounts")
public class AccountManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;
    private final AccountServiceImpl accountService;

    public AccountManagementController(SecurityContextHelper securityContextHelper,
                                      AccountServiceImpl accountService,
                                      AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.accountService = accountService;
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

            //Map<String, Object> result = adminUserManagementService.createAccount(
            //        request.getParentAccountId(),
            //        request.getName(),
            //        request.getNodeType()
            //);

            //Map<String, Object> response = new HashMap<>();
            //response.put("id", result.get("id"));
            //response.put("name", result.get("name"));
            //response.put("nodeType", result.get("nodeType"));
            //response.put("parentId", result.get("parentId"));

            return buildCreatedResponse(null, "Account created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Get /api/v1/{tenantCode}/admin/accounts/{accuntId}
    */
    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable String tenantCode, @PathVariable Long accountId) {
        try {
            requireAnyRole("SYS_ADMIN", "GROUP_ADMIN", "PRODUCT_ADMIN");
            //Map<String, Object> account = adminUserManagementService.getAccount(accountId);
            //return ResponseEntity.ok(account);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /** Get /api/v1/{tenantCode}/admin/accounts/{accountId}/accounts
    */
    @GetMapping("/{accountId}/accounts")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<?> getAccountAccounts(@PathVariable String tenantCode, @PathVariable Long accountId) {
        try {
            requireAnyRole("SYS_ADMIN", "GROUP_ADMIN", "PRODUCT_ADMIN");
            //List<Map<String, Object>> accounts = adminUserManagementService.getAccountAccounts(accountId);
            //return ResponseEntity.ok(accounts);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Создание аккаунта
     * POST /api/v1/{tenantCode}/admin/accounts
     */
    @PostMapping("/{accountId}/accounts")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createAccountAccount(
            @PathVariable String tenantCode, @PathVariable Long accountId, @RequestBody CreateAccountRequest request) {
        try {
            requireAnyRole("SYS_ADMIN", "GROUP_ADMIN", "PRODUCT_ADMIN");

            //Map<String, Object> result = adminUserManagementService.createAccount(
            //        accountId,
            //        request.getName(),
            //        request.getNodeType()
            //);

            //Map<String, Object> response = new HashMap<>();
            //response.put("id", result.get("id"));
            //response.put("name", result.get("name"));
            //response.put("nodeType", result.get("nodeType"));
            //response.put("parentId", result.get("parentId"));

            return buildCreatedResponse(null, "Account created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * GROUP_ADMIN / PRODUCT_ADMIN: Получить иерархию аккаунтов
     * GET /api/v1/{tenantCode}/admin/accounts/hierarchy
     */
    @GetMapping("/hierarchy")
    @PreAuthorize("hasAnyRole('SYS_ADMIN', 'GROUP_ADMIN', 'PRODUCT_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAccountsHierarchy(@PathVariable String tenantCode) {
        try {
            requireAnyRole("SYS_ADMIN", "GROUP_ADMIN", "PRODUCT_ADMIN");

            //List<Map<String, Object>> hierarchy = adminUserManagementService.getAccountsHierarchy();
            //return ResponseEntity.ok(hierarchy);
            return ResponseEntity.ok(null);
            //return ResponseEntity.ok(hierarchy);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/createSubaccount")
    public ResponseEntity<Account> createSubaccount(
            @PathVariable("tenantCode") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createSubaccount(account.name(), account.parentId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/getProductRoles/{accountId}")
    public ResponseEntity<Object> getProductRoles(
            @PathVariable("tenantCode") String tenantCode,
            @PathVariable("accountId") long accountId) {
        Set<String> result = accountService.getProductRoles(accountId);
        return ResponseEntity.ok(result.toArray());
    }


    // DTO Classes
    public static class CreateAccountRequest {

    
        private Long parentAccountId;
        private String name;
        private String nodeType;

        public Long getParentAccountId() {
            return parentAccountId;
        }

        public void setParentAccountId(Long parentAccountId) {
            this.parentAccountId = parentAccountId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }
    }
}


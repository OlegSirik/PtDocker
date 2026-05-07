package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.AccountLogin;
import ru.pt.api.dto.auth.AccountToken;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.model.AdminResponse;
import ru.pt.api.security.SecuredController;
import ru.pt.api.service.auth.AccountLoginService;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.auth.AccountTokenService;
import ru.pt.api.dto.exception.UnauthorizedException;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.admin.AdminManagementService;
import lombok.AllArgsConstructor;
import java.util.List;

//import ru.pt.api.admin.deleted.AdminManagementController;

/**
 * Контроллер для управления аккаунтами
 * Доступен для GROUP_ADMIN и PRODUCT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/accounts
 * tenantCode: pt, vsk, msg
 * 
 * Get /v1/{tenantCode}/admin/accounts/{accuntId}
 * Get /v1/{tenantCode}/admin/accounts/{accuntId}/accounts
 * Post /v1/{tenantCode}/admin/accounts/{accuntId}/accounts
 * Get /v1/{tenantCode}/admin/accounts/{accuntId}/subs
 * Post /v1/{tenantCode}/admin/accounts/{accuntId}/subs
 * Get /v1/{tenantCode}/admin/accounts/{accuntId}/groups
 * Post /v1/{tenantCode}/admin/accounts/{accuntId}/groups
 */
@RestController
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/accounts")
public class AccountManagementController {

    private final AdminManagementService adminManagementService;
    private final AccountService accountService;
    private final AccountTokenService accountTokenService;
    private final AccountLoginService accountLoginService;
    private final SecurityContextHelper securityContextHelper;

    /**
     * public record Account(
        Long id,
        Long tid,
        Long clientId,
        Long parentId,
        String nodeType,
        String name
        ) {}
     */
//    AccountNodeType.ACCOUNT, AccountNodeType.GROUP, AccountNodeType.SUB, AccountNodeType.TOKEN  

    /**
     * Get /v1/{tenantCode}/admin/accounts/{accountId}/children?nodeType=
     */
    @GetMapping("/{accountId}/children")
    public ResponseEntity<List<Account>> getAccountChildren(
            @PathVariable String tenantCode,
            @PathVariable Long accountId,
            @RequestParam(required = false) String nodeType) {
        List<Account> children = accountService.getChildren(accountId, nodeType);
        return ResponseEntity.ok(children);
    }

    /**
     * POST /v1/{tenantCode}/admin/accounts/{accountId}/children
     * Body: { "name": "...", "nodeType": "GROUP"|"ACCOUNT"|"SUB" }
     */
    @PostMapping("/{accountId}/children")
    public ResponseEntity<Account> postAccountChildren(
            @PathVariable String tenantCode,
            @PathVariable Long accountId,
            @RequestBody CreateAccountChild request) {
        Account account = accountService.createChild(request.name(), request.nodeType(), accountId);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }

    /** Get /v1/{tenantCode}/admin/accounts/{accuntId}
    */
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String tenantCode, @PathVariable Long accountId) {
        var currentUser = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        Long tenantId = currentUser.getTenantId();
        Account account = accountService.getAccountById(tenantId, accountId);
        return ResponseEntity.ok(account);
    }

    /** Get /v1/{tenantCode}/admin/accounts/{accuntId}
    */
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String tenantCode, @PathVariable Long accountId) {
        var currentUser = securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
        Long tenantId = currentUser.getTenantId();
        accountService.deleteAccount(tenantId, accountId);
        return ResponseEntity.noContent().build();
    }
    /*
     * POST /api/v1/{tenantCode}/admin/accounts/{accountId}/accounts
     */
    /* 
    @PostMapping("/{accountId}/accounts")
    public ResponseEntity<Account> createAccountAccount(
            @PathVariable String tenantCode, @PathVariable Long accountId, @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(request.getName(), accountId);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }
*/
    /**
     * Delete GROUP_ADMIN user
     * DELETE /api/v1/{tenantCode}/admin/accounts/{accountId}/group-admins/{accountLoginId}
     */
    @DeleteMapping("/{accountId}/group_admins/{accountLoginId}")
    public ResponseEntity<Void> deleteGroupAdmin(
            @PathVariable String tenantCode, @PathVariable Long accountId,
            @PathVariable Long accountLoginId) {
        adminManagementService.deleteAccountMember(accountId, accountLoginId);
        return ResponseEntity.noContent().build();
    }

/* 
    @PostMapping("/{accountId}/subaccounts")
    public ResponseEntity<Account> createSubaccount(
        @PathVariable String tenantCode, 
        @PathVariable Long accountId, 
        @RequestBody CreateAccountRequest request) {

        Account account = accountService.createSubaccount(request.getName(), accountId);
        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }
*/
    /** Get /api/v1/{tenantCode}/admin/accounts/{accountId}/tokens
     * Get subaccounts under the specified account
    */
    @GetMapping("/{accountId}/tokens")
    public ResponseEntity<List<AccountToken>> getAccountTokens(@PathVariable String tenantCode, @PathVariable Long accountId) {
        List<AccountToken> tokens = accountTokenService.getTokens(accountId);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/{accountId}/tokens")
    public ResponseEntity<AccountToken> createToken(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId, 
            @RequestBody CreateTokenRequest request) {
        AccountToken token = accountTokenService.createToken(accountId, request.getToken());
        return new ResponseEntity<>(token, HttpStatus.CREATED);
    }

    @DeleteMapping("/{accountId}/tokens/{token}")
    public ResponseEntity<Void> deleteToken(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId, 
            @PathVariable String token) {
        accountTokenService.deleteToken(accountId, token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all logins for the account
     * GET /api/v1/{tenantCode}/admin/accounts/{accountId}/logins
     */
    @GetMapping("/{accountId}/logins")
    public ResponseEntity<List<AccountLogin>> getAccountLogins(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId) {
        List<AccountLogin> logins = accountLoginService.getLoginsByAccountId(accountId);
        return ResponseEntity.ok(logins);
    }

    /**
     * Create login binding for the account
     * POST /api/v1/{tenantCode}/admin/accounts/{accountId}/logins
     */
    @PostMapping("/{accountId}/logins")
    public ResponseEntity<AccountLogin> createLogin(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId, 
            @RequestBody AccountLogin login) {
        AccountLogin createdLogin = accountLoginService.createLogin(accountId, login);
        return new ResponseEntity<>(createdLogin, HttpStatus.CREATED);
    }

    /**
     * Delete login binding from the account
     * DELETE /api/v1/{tenantCode}/admin/accounts/{accountId}/logins/{userLogin}
     */
    @DeleteMapping("/{accountId}/logins/{userLogin}")
    public ResponseEntity<Void> deleteLogin(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId, 
            @PathVariable String userLogin) {
        accountLoginService.deleteLogin(accountId, userLogin);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get path from acting account to specified account
     * GET /api/v1/{tenantCode}/admin/accounts/{accountId}/path
     */
    @GetMapping("/{accountId}/path")    
    public ResponseEntity<List<Account>> getAccountsHierarchy(
        @PathVariable String tenantCode, @PathVariable Long accountId) {
            List<Account> list = accountService.getPathToRoot(accountId);
            return ResponseEntity.ok(list);
    }

    /**
     * Get all product roles for the account
     * GET /api/v1/{tenantCode}/admin/accounts/{accountId}/products
     */
    @GetMapping("/{accountId}/products")
    public ResponseEntity<List<ProductRole>> getAccountProducts(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId) {
        List<ProductRole> productRoles = accountService.getProductRolesByAccountId(accountId);
        return ResponseEntity.ok(productRoles);
    }

    /**
     * Get specific product role for the account
     * GET /api/v1/{tenantCode}/admin/accounts/{accountId}/products/{productId}
     */
    @GetMapping("/{accountId}/products/{productId}")
    public ResponseEntity<ProductRole> getAccountProduct(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId,
            @PathVariable Long productId) {
        ProductRole productRole = accountService.getProductRole(accountId, productId); 
        return ResponseEntity.ok(productRole);
    }

    /**
     * Grant or update product permissions for the account
     * POST /api/v1/{tenantCode}/admin/accounts/{accountId}/products
     */
    @PostMapping("/{accountId}/products")
    public ResponseEntity<ProductRole> postAccountProduct(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId,
            @RequestBody ProductRole productRole) {
        ProductRole savedRole = accountService.grantProduct(accountId, productRole);
        return ResponseEntity.ok(savedRole);
    }

    @PutMapping("/{accountId}/products")
    public ResponseEntity<ProductRole> putAccountProduct(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId,
            @RequestBody ProductRole productRole) {
        ProductRole savedRole = accountService.grantProduct(accountId, productRole);
        return ResponseEntity.ok(savedRole);
    }
    /**
     * Revoke product permissions from the account
     * DELETE /api/v1/{tenantCode}/admin/accounts/{accountId}/products/{productId}
     */
    @DeleteMapping("/{accountId}/products/{productId}")
    public ResponseEntity<Void> deleteAccountProduct(
            @PathVariable String tenantCode, 
            @PathVariable Long accountId,
            @PathVariable Long productId) {
        accountService.revokeProduct(accountId, productId);
        return ResponseEntity.noContent().build();
    }

    // DTO Classes
    public static record CreateAccountChild(String name, String nodeType) {} 

    public static record AccountUser(Long id, String role, String login, String name, String position) {}

    public static class CreateAccountRequest {
        private Long parentAccountId;
        private String name;
        private String nodeType;

        public Long getParentAccountId() { return parentAccountId; }
        public void setParentAccountId(Long parentAccountId) { this.parentAccountId = parentAccountId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getNodeType() { return nodeType; }
        public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    }

    public static class CreateTokenRequest {
        private String token;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class AddGroupAdminRequest {
        private String userLogin;

        public String getUserLogin() { return userLogin; }
        public void setUserLogin(String userLogin) { this.userLogin = userLogin; }
    }

    public static class CreateGroupAdminRequest {
        private String userLogin;
        private String groupName;
        private String fullName;

        public String getUserLogin() { return userLogin; }
        public void setUserLogin(String userLogin) { this.userLogin = userLogin; }
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}


package ru.pt.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер для управления обычными пользователями
 * Доступен для PRODUCT_ADMIN
 */
@RestController
@RequestMapping("/api/admin/users")
public class UserManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;

    public UserManagementController(SecurityContextHelper securityContextHelper,
                                   AdminUserManagementService adminUserManagementService) {
        super(securityContextHelper);
        this.adminUserManagementService = adminUserManagementService;
    }

    /**
     * PRODUCT_ADMIN: Создание обычного пользователя (USER роль)
     * POST /api/admin/users
     */
    @PostMapping
    @PreAuthorize("hasRole('PRODUCT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody CreateUserRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.createUser(
                    request.getUserLogin(),
                    request.getAccountId(),
                    request.getUserRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());
            response.put("accountId", accountLogin.getAccount().getId());
            response.put("accountName", accountLogin.getAccount().getName());

            return buildCreatedResponse(response, "User created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * PRODUCT_ADMIN: Обновление роли пользователя
     * PUT /api/admin/users/{accountLoginId}
     */
    @PutMapping("/{accountLoginId}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long accountLoginId,
            @RequestBody UpdateUserRequest request) {
        try {
            AccountLoginEntity accountLogin = adminUserManagementService.updateUser(
                    accountLoginId,
                    request.getUserRole()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", accountLogin.getId());
            response.put("userLogin", accountLogin.getUserLogin());
            response.put("userRole", accountLogin.getUserRole());

            return buildSuccessResponse(response, "User updated successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    /**
     * PRODUCT_ADMIN: Удаление пользователя
     * DELETE /api/admin/users/{accountLoginId}
     */
    @DeleteMapping("/{accountLoginId}")
    @PreAuthorize("hasRole('PRODUCT_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long accountLoginId) {
        try {
            adminUserManagementService.deleteUser(accountLoginId);

            Map<String, Object> response = buildSimpleResponse("User deleted successfully");
            response.put("accountLoginId", accountLoginId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    // DTO Classes
    public static class CreateUserRequest {
        private String userLogin;
        private Long accountId;
        private String userRole;

        public String getUserLogin() {
            return userLogin;
        }

        public void setUserLogin(String userLogin) {
            this.userLogin = userLogin;
        }

        public Long getAccountId() {
            return accountId;
        }

        public void setAccountId(Long accountId) {
            this.accountId = accountId;
        }

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }
    }

    public static class UpdateUserRequest {
        private String userRole;

        public String getUserRole() {
            return userRole;
        }

        public void setUserRole(String userRole) {
            this.userRole = userRole;
        }
    }
}


package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AccountServiceImpl;
import ru.pt.auth.service.AdminUserManagementService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления клиентами (приложениями)
 * Доступен для TNT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/clients
 * tenantCode: pt, vsk, msg
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/clients")
public class ClientManagementController extends SecuredController {

    private final AdminUserManagementService adminUserManagementService;
    private final AccountServiceImpl accountService;

    public ClientManagementController(SecurityContextHelper securityContextHelper,
                                      AdminUserManagementService adminUserManagementService,
                                      AccountServiceImpl accountService
    ) {
        super(securityContextHelper);
        this.accountService = accountService;
        this.adminUserManagementService = adminUserManagementService;
    }

    /**
     * TNT_ADMIN: Создание нового клиента (приложения)
     * POST /api/v1/{tenantCode}/admin/clients
     */
    @PostMapping
    @PreAuthorize("hasRole('TNT_ADMIN')")
    public ResponseEntity<Map<String, Object>> createClient(
            @PathVariable String tenantCode,
            @RequestBody CreateClientRequest request) {
        try {
            Map<String, Object> result = adminUserManagementService.createClient(
                    request.getClientId(),
                    request.getClientName()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("id", result.get("id"));
            response.put("clientId", result.get("clientId"));
            response.put("name", result.get("name"));

            return buildCreatedResponse(response, "Client created successfully");
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/createGroup")
    @PreAuthorize("hasRole('TNT_ADMIN')")
    public ResponseEntity<Account> createGroup(
            @PathVariable("tenantCode") String tenantCode,
            @RequestBody Account account) {
        Account result = accountService.createGroup(account.getName(), account.getParentId());
        return ResponseEntity.ok(result);
    }

    /**
     * TNT_ADMIN: Получить список всех клиентов
     * GET /api/v1/{tenantCode}/admin/clients
     */
    @GetMapping
    @PreAuthorize("hasRole('TNT_ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> listClients(@PathVariable String tenantCode) {
        try {
            List<Map<String, Object>> clients = adminUserManagementService.listClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.status(getForbiddenStatus()).build();
        }
    }

    private org.springframework.http.HttpStatus getForbiddenStatus() {
        return org.springframework.http.HttpStatus.FORBIDDEN;
    }

    // DTO Classes
    public static class CreateClientRequest {
        private String clientId;
        private String clientName;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientName() {
            return clientName;
        }

        public void setClientName(String clientName) {
            this.clientName = clientName;
        }
    }
}


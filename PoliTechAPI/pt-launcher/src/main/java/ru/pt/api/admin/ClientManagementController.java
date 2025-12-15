package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Account;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.AccountServiceImpl;
import ru.pt.auth.service.AdminUserManagementService;
import ru.pt.auth.entity.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import ru.pt.auth.security.UserDetailsImpl;
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
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Client> createClient(
            @PathVariable String tenantCode,
            @RequestBody Client request) {
        try {
            if (!getCurrentUser().getTenantCode().equals(tenantCode)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Client newClient = adminUserManagementService.createClient(request);

            return ResponseEntity.ok(newClient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
    // TODO remove SYS_ADMIN
    @GetMapping
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<List<Client>> listClients(@PathVariable String tenantCode) {
        try {
            List<ClientEntity> clients = adminUserManagementService.listClients();
            List<Client> clientsDto = clients.stream()
                .map(client -> {
                    //ClientConfiguration configuration = clientConfigurationService.getClientConfiguration(client.getId());
                    Client dto = new Client();
                    dto.setId(client.getId());
                    dto.setTid(client.getTenant().getId());
                    dto.setClientId(client.getClientId());
                    dto.setName(client.getName());
                    dto.setIsDeleted(client.getDeleted());
                    dto.setCreatedAt(client.getCreatedAt());
                    dto.setUpdatedAt(client.getUpdatedAt());
                    dto.setTid(client.getTenant().getId());
                    dto.setDefaultAccountId(client.getDefaultAccountId());
                    ClientConfigurationEntity conf = client.getClientConfigurationEntity();
                    if (conf != null) {
                     
                    ClientConfiguration configuration = new ClientConfiguration();
                        configuration.setPaymentGate(conf.getPaymentGate());
                        configuration.setSendEmailAfterBuy(conf.isSendEmailAfterBuy());
                        configuration.setSendSmsAfterBuy(conf.isSendSmsAfterBuy());
                        configuration.setPaymentGateAgentNumber(conf.getPaymentGateAgentNumber());
                        configuration.setPaymentGateLogin(conf.getPaymentGateLogin());
                        configuration.setPaymentGatePassword(conf.getPaymentGatePassword());
                        configuration.setEmployeeEmail(conf.getEmployeeEmail());
                    
                        dto.setClientConfiguration(configuration);
                    }
                    return dto;
                })
                .collect(Collectors.toList());
            return ResponseEntity.ok(clientsDto);
        } catch (Exception e) {
            return ResponseEntity.status(getForbiddenStatus()).build();
        }
    }

    /**
     * TNT_ADMIN: Получить клиента по ID
     * GET /api/v1/{tenantCode}/admin/clients/{clientId}
     */
    // TODO remove SYS_ADMIN
    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Client> getClient(@PathVariable String tenantCode, @PathVariable Long clientId) {
        try {
            Client client = adminUserManagementService.getClientById(clientId);
            return ResponseEntity.ok(client);
        } catch (Exception e) {
            return ResponseEntity.status(getForbiddenStatus()).build();
        }
    }

    private org.springframework.http.HttpStatus getForbiddenStatus() {
        return org.springframework.http.HttpStatus.FORBIDDEN;
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Client> updateClient(@PathVariable String tenantCode, @PathVariable Long clientId, @RequestBody Client request) {
        try {
            if (!clientId.equals(request.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            Client updatedClient = adminUserManagementService.updateClient(request);
            return ResponseEntity.ok(updatedClient);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    protected UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }
}


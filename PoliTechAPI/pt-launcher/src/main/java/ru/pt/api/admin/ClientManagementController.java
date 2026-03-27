package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.auth.service.ClientService;
import lombok.RequiredArgsConstructor;
import ru.pt.auth.service.admin.AdminManagementService;
import java.util.List;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.model.AdminResponse;
import ru.pt.api.service.auth.AccountService;


/**
 * Контроллер для управления клиентами (приложениями)
 * Доступен для TNT_ADMIN
 *
 * URL Pattern: /api/v1/{tenantCode}/admin/clients
 * tenantCode: pt, vsk, msg
 */
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/clients")
public class ClientManagementController {

    private final ClientService clientService;
    private final AdminManagementService adminManagementService;
    private final AccountService accountService;

    /**
     * TNT_ADMIN: Создание нового клиента (приложения)
     * POST /api/v1/{tenantCode}/admin/clients
     */
    @PostMapping
    public ResponseEntity<Client> createClient(
            @RequestBody Client request) {
        
        Client newClient = clientService.createClient(request);
        return ResponseEntity.ok(newClient); 
    }

    /**
     * TNT_ADMIN: Получить список всех клиентов
     * GET /api/v1/{tenantCode}/admin/clients
     */
    @GetMapping
    public ResponseEntity<List<Client>> listClients() { 
            return ResponseEntity.ok(clientService.listClients());
    }

    /**
     * TNT_ADMIN: Получить клиента по ID
     * GET /api/v1/{tenantCode}/admin/clients/{clientId}
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<Client> getClient(@PathVariable Long clientId) {
        Client client = clientService.getClientById(clientId);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<Client> updateClient(@PathVariable Long clientId, @RequestBody Client request) {  
        Client updatedClient = clientService.updateClient(request);
        return ResponseEntity.ok(updatedClient);  
    }

    /**
     * Client products
     */
    @GetMapping("/{clientId}/products")
    public ResponseEntity<List<ProductRole>> listClientProducts
        ( @PathVariable Long clientId
        ) {
            List<ProductRole> products = clientService.listClientProducts(clientId);
            return ResponseEntity.ok(products);
    }

    @PostMapping("/{clientId}/products")
    public ResponseEntity<Void> grantClientProduct
        ( @RequestBody ProductRole productRole
        , @PathVariable Long clientId
    ) {
        clientService.grantProduct(clientId, productRole);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{clientId}/products/{productRoleId}")
    public ResponseEntity<Void> revokeClientProduct
        ( @PathVariable Long clientId
        , @PathVariable Long productRoleId
    ) {
        clientService.revokeProduct(clientId, productRoleId);
        return ResponseEntity.accepted().build();
    }
    
    @GetMapping("/{clientId}/members")
    public ResponseEntity<List<AdminResponse>> getClientMembers(
            @PathVariable String tenantCode,
            @PathVariable Long clientId,
        @RequestParam(required = false) String role
    ) {
        Client client = clientService.getClientById(clientId);
        if (client.getClientAccountId() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<AdminResponse> members = adminManagementService.getAccountMembers( client.getClientAccountId(), role == null ? null : UserRole.valueOf(role.toUpperCase()).toString() );
        return ResponseEntity.ok(members);
    }

    @PostMapping("/{clientId}/members")
    public ResponseEntity<AdminResponse> addMember(
            @PathVariable Long clientId,
            @RequestBody AddMemberRequest request) {
        Client client = clientService.getClientById(clientId);
        if (client.getClientAccountId() == null) {
            return ResponseEntity.badRequest().build();
        }
        AdminResponse member = adminManagementService.setAccountMember(
                client.getClientAccountId(), request.role(), request.userLogin());
        return ResponseEntity.ok(member);
    }

    /**
     * {@code accountId} — из {@link AdminResponse#accountId()} (ролевой узел, например GROUP_ADMIN).
     */
    @DeleteMapping("/{clientId}/members/{memberId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable Long clientId,
            @PathVariable Long memberId ) {
        Client client = clientService.getClientById(clientId);
        if (client.getClientAccountId() == null) {
            return ResponseEntity.badRequest().build();
        }
        var account = accountService.getRoleAccount("GROUP_ADMIN", client.getClientAccountId());
        if (account == null) {
            return ResponseEntity.badRequest().build();
        }
        adminManagementService.deleteAccountMember(
            account.id(), memberId);
        return ResponseEntity.noContent().build();
    }

    public record AddMemberRequest(String role, String userLogin) {}
}

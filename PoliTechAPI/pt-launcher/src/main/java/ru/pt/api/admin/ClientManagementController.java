package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.ClientAdminsManagementService;
import ru.pt.auth.service.ClientService;

import java.util.List;



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

    private final ClientService clientService;

    public ClientManagementController(SecurityContextHelper securityContextHelper,
                                      ClientService clientService,
                                      ClientAdminsManagementService clientAdminsManagementService) {
        super(securityContextHelper);
        this.clientService = clientService;
    }

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
    
    
}


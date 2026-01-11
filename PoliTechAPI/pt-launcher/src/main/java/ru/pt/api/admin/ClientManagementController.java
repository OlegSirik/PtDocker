package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.security.SecuredController;
import ru.pt.auth.entity.ClientConfigurationEntity;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.ClientService;

import java.util.List;
import java.util.stream.Collectors;

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
                                      ClientService clientService) {
        super(securityContextHelper);
        this.clientService = clientService;
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
        
        Client newClient = clientService.createClient(request);
        return ResponseEntity.ok(newClient); 
    }


    /**
     * TNT_ADMIN: Получить список всех клиентов
     * GET /api/v1/{tenantCode}/admin/clients
     */
    @GetMapping
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<List<Client>> listClients(@PathVariable String tenantCode) {
        
            List<ClientEntity> clients = clientService.listClients();
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
                    dto.setAuthType(client.getAuthType());
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
    }

    /**
     * TNT_ADMIN: Получить клиента по ID
     * GET /api/v1/{tenantCode}/admin/clients/{clientId}
     */
    @GetMapping("/{clientId}")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Client> getClient(@PathVariable String tenantCode, @PathVariable Long clientId) {
        Client client = clientService.getClientById(clientId);
        return ResponseEntity.ok(client);
    }


    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<Client> updateClient(@PathVariable String tenantCode, @PathVariable Long clientId, @RequestBody Client request) {  
        Client updatedClient = clientService.updateClient(request);
        return ResponseEntity.ok(updatedClient);  
    }

}


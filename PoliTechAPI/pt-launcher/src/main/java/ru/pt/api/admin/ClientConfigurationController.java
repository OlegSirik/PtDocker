package ru.pt.api.admin;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.auth.service.ClientConfigurationService;

/**
 * Конфигурация для настройки интеграции(оплата + колбэки после оплаты)
 */
@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("/api/v1/{tenantCode}/admin/clients/configuration")
public class ClientConfigurationController {

    private final ClientConfigurationService clientConfigurationService;

    public ClientConfigurationController(ClientConfigurationService clientConfigurationService) {
        this.clientConfigurationService = clientConfigurationService;
    }


    @PostMapping("/{clientId}")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<ClientConfiguration> create(
            @PathVariable("clientId") Long clientId,
            @RequestBody ClientConfiguration clientConfiguration) {
        return ResponseEntity.ok(
                clientConfigurationService.saveClientConfiguration(clientId, clientConfiguration)
        );
    }

    @PutMapping("/{clientId}")
    @PreAuthorize("hasRole('TNT_ADMIN') or hasRole('SYS_ADMIN')")
    public ResponseEntity<ClientConfiguration> update(
            @PathVariable("clientId") Long clientId,
            @RequestBody ClientConfiguration clientConfiguration
    ) {
        return ResponseEntity.ok(
                clientConfigurationService.saveClientConfiguration(clientId, clientConfiguration)
        );
    }

}

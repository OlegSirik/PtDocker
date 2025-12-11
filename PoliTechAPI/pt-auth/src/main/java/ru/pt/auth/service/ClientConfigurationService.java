package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.ClientConfigurationEntity;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.repository.ClientRepository;

@Service
public class ClientConfigurationService {

    private final AdminUserManagementService userService;
    private final ClientRepository clientRepository;
    private final ClientConfigurationMapper clientConfigurationMapper;

    public ClientConfigurationService(AdminUserManagementService userService,
                                      ClientRepository clientRepository,
                                      ClientConfigurationMapper clientConfigurationMapper) {
        this.userService = userService;
        this.clientRepository = clientRepository;
        this.clientConfigurationMapper = clientConfigurationMapper;
    }

    @Transactional
    public ClientConfiguration saveClientConfiguration(Long clientId, ClientConfiguration clientConfiguration) {
        Client client = userService.getClientById(clientId);
        ClientEntity clientEntity = clientRepository.findById(client.getId())
                .orElseThrow(() -> new NotFoundException("Client not found: " + client.getId()));

        ClientConfigurationEntity configurationEntity = clientEntity.getClientConfigurationEntity();
        if (configurationEntity == null) {
            configurationEntity = clientConfigurationMapper.toEntity(clientConfiguration);
        } else {
            configurationEntity.setPaymentGate(clientConfiguration.getPaymentGate());
            configurationEntity.setSendEmailAfterBuy(clientConfiguration.isSendEmailAfterBuy());
            configurationEntity.setSendSmsAfterBuy(clientConfiguration.isSendSmsAfterBuy());
            configurationEntity.setPaymentGateAgentNumber(clientConfiguration.getPaymentGateAgentNumber());
            configurationEntity.setPaymentGateLogin(clientConfiguration.getPaymentGateLogin());
            configurationEntity.setPaymentGatePassword(clientConfiguration.getPaymentGatePassword());
            configurationEntity.setEmployeeEmail(clientConfiguration.getEmployeeEmail());
        }

        clientEntity.setClientConfigurationEntity(configurationEntity);
        ClientEntity savedClient = clientRepository.save(clientEntity);

        return clientConfigurationMapper.toDto(savedClient.getClientConfigurationEntity());
    }
}

package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.model.ClientSecurityConfig;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.ClientRepository;

import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountLoginRepository accountLoginRepository;

    public ClientService(ClientRepository clientRepository, AccountLoginRepository accountLoginRepository) {
        this.clientRepository = clientRepository;
        this.accountLoginRepository = accountLoginRepository;
    }

    public Optional<ClientEntity> findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    /**
     * Получает логин для базового аккаунта клиента
     */
    public Optional<String> getDefaultAccountLogin(Long defaultAccountId) {
        // Ищем запись в acc_account_logins для базового аккаунта
        return accountLoginRepository.findByAccountId(defaultAccountId)
                .map(AccountLoginEntity::getUserLogin);
    }

    /**
     * Создает базовый аккаунт для клиента
     */
    public void createDefaultAccountForClient(ClientEntity client, String defaultUserLogin) {
        // Здесь должна быть логика создания базового аккаунта
        // Этот метод будет вызываться при создании нового клиента

        // TODO: Реализовать создание AccountLoginEntity для базового пользователя
        // и установить client.setDefaultAccountId() после создания
    }

    public ClientSecurityConfig getConfig(String tenantCode, String authClientId) {
        return clientRepository.findByTenantCodeAndAuthClientId(tenantCode, authClientId)
                .map(this::mapToDomain)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + tenantCode + " " + authClientId));
    }

    private ClientSecurityConfig mapToDomain(ClientEntity e) {
        return new ClientSecurityConfig(
            e.getId(),
            e.getClientId(),
            e.getDefaultAccountId(),
            e.getTenant().getId(),
            e.getName()
        );
    }
}

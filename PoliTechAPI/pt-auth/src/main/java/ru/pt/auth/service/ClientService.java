package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import ru.pt.auth.entity.ClientEntity;
import ru.pt.auth.repository.ClientRepository;

import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<ClientEntity> findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId);
    }


}

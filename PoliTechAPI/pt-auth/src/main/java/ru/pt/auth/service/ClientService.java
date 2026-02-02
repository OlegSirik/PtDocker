package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.pt.api.dto.auth.Client;
import ru.pt.api.dto.auth.ClientAuthType;
import ru.pt.api.dto.auth.ClientConfiguration;
import ru.pt.api.dto.auth.ProductRole;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.entity.*;
import ru.pt.auth.model.ClientSecurityConfig;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.ClientRepository;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.utils.ClientMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final AccountRepository accountRepository;
    private final TenantService tenantService;
    private final SecurityContextHelper securityContextHelper;
    private final ClientMapper clientMapper;
    private final AccountService accountService;
    private final ProductService productService;

    public ClientService(
            ClientRepository clientRepository,
            AccountLoginRepository accountLoginRepository,
            AccountRepository accountRepository,
            TenantService tenantService,
            SecurityContextHelper securityContextHelper,
            AccountService accountService,
            ProductService productService) {
        this.clientRepository = clientRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.accountRepository = accountRepository;
        this.tenantService = tenantService;
        this.securityContextHelper = securityContextHelper;
        this.clientMapper = new ClientMapper();
        this.accountService = accountService;
        this.productService = productService;
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
            e.getName(),
            ClientAuthType.valueOf(e.getAuthType())
        );
    }

    // ========== CLIENT MANAGEMENT (TNT_ADMIN) ==========

    /**
     * TNT_ADMIN: Создание нового клиента
     */
    @Transactional
    public Client createClient(Client client) {
        UserDetailsImpl currentUser = getCurrentUser();

        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can create clients");
        }

        TenantEntity tenant = tenantService.findByCode(currentUser.getTenantCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Проверка уникальности clientId
        if (clientRepository.findByClientIdandTenantCode(client.getClientId(), currentUser.getTenantCode()).isPresent()) {
            throw new BadRequestException("Client with ID '" + client.getClientId() + "' already exists");
        }

        client.setTid(tenant.getId());

        if (client.getClientConfiguration() == null) {
            ClientConfiguration clientConfiguration = new ClientConfiguration();
            client.setClientConfiguration(clientConfiguration);
        }

        ClientEntity clientEntity = clientMapper.toEntity(client);
        clientEntity.setId(accountRepository.getNextAccountId());
        ClientEntity saved = clientRepository.save(clientEntity);

        AccountEntity tenantAccount = accountRepository.findByTenantId(tenant.getId())
                .orElseThrow(() -> new NotFoundException("Tenant account not found for tenant id: " + tenant.getId()));

        AccountEntity account = AccountEntity.clientAccount(clientEntity, tenantAccount);
        account.setId(saved.getId());
        AccountEntity savedAccount = accountRepository.save(account);

        // default account
        AccountEntity defAccount = AccountEntity.defaultClientAccount(savedAccount);
        defAccount.setId(accountRepository.getNextAccountId());
        AccountEntity savedDefAccount = accountRepository.save(defAccount);

        saved.setDefaultAccountId(savedDefAccount.getId());
        saved = clientRepository.save(saved);

        Client clientDto = clientMapper.toDto(saved);
        return clientDto;
    }

    /**
     * TNT_ADMIN: Обновление клиента
     */
    @Transactional
    public Client updateClient(Client client) {
        UserDetailsImpl currentUser = getCurrentUser();

        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can update clients");
        }

        TenantEntity tenant = tenantService.findByCode(currentUser.getTenantCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        ClientEntity clientToUpdate = clientRepository.findById(client.getId())
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if ("TNT_ADMIN".equals(currentUser.getUserRole())) {
            // Если это sys_admin, то можно обновить любого клиента, если это tnt_admin то только из своего тенанта
            // Проверка, что текущий пользователь имеет доступ к этой записи
            if (!clientToUpdate.getTenant().getCode().equals(tenant.getCode())) {
                throw new ForbiddenException("You do not have access to this tenant data");
            }
        }

        if (!clientToUpdate.getClientId().equals(client.getClientId())) {
            // Проверка уникальности нового clientId
            ClientEntity oldClient = clientRepository.findByClientIdandTenantCode(client.getClientId(), currentUser.getTenantCode())
                    .orElse(new ClientEntity());

            if (oldClient.getId() != null && !oldClient.getId().equals(client.getId())) {
                throw new BadRequestException("Client with ID '" + client.getClientId() + "' already exists");
            }
        }

        //client.setUpdatedAt(LocalDateTime.now());
        client.setTid(tenant.getId());

        ClientEntity clientEntity = clientMapper.toEntity(client);
        ClientEntity saved = clientRepository.save(clientEntity);

        AccountEntity account = accountRepository.findCliensAccountByClientId(saved.getId())
                .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + saved.getClientId()));

        Client clientDto = clientMapper.toDto(saved);
        clientDto.setClientAccountId(account.getId());
        return clientDto;
    }

    /**
     * TNT_ADMIN: Получить список всех клиентов
     */
    @Transactional(readOnly = true)
    public List<Client> listClients() {

        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can list clients");
        }

        List<ClientEntity> clients = clientRepository.findBytenantCode(currentUser.getTenantCode());

        List<Client> clientsDto = clients.stream()
            .map(client -> {
                //ClientConfiguration configuration = clientConfigurationService.getClientConfiguration(client.getId());
                Client dto = new Client();
                dto.setId(client.getId());
                dto.setTid(client.getTenant().getId());
                dto.setClientId(client.getClientId());
                dto.setName(client.getName());
                dto.setIsDeleted(client.getDeleted());
                //dto.setCreatedAt(client.getCreatedAt());
                //dto.setUpdatedAt(client.getUpdatedAt());
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

        return clientsDto;
    }

    /**
     * TNT_ADMIN: Получить клиента по ID
     */
    @Transactional(readOnly = true)
    public Client getClientById(Long id) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"TNT_ADMIN".equals(currentUser.getUserRole()) && !"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only TNT_ADMIN can get clients");
        }

        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        if (!client.getTenant().getCode().equals(currentUser.getTenantCode())) {
            throw new ForbiddenException("Cannot access client from different tenant");
        }

        AccountEntity account = accountRepository.findCliensAccountByClientId(id)
                .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + id));

        Client clientDto = clientMapper.toDto(client);
        clientDto.setClientAccountId(account.getId());
        return clientDto;
    }

    // ========== HELPER METHODS ==========

    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }

    @Transactional(readOnly = true)
    public List<ProductRole> listClientProducts(Long clientId) {
        UserDetailsImpl currentUser = getCurrentUser();
        AccountEntity account = accountRepository.findClientAccountById(currentUser.getTenantCode(), clientId)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + clientId));

        return accountService.getProductRolesByAccountId(account.getId());
        //accountService.getProd
    }

    @Transactional
    public void grantProduct(Long clientId, ProductRole productRole) {
        UserDetailsImpl currentUser = getCurrentUser();
        AccountEntity account = accountRepository.findClientAccountById(currentUser.getTenantCode(), clientId)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + clientId));

        // set all boolean columns like can% to true
        ProductRole updatedProductRole = new ProductRole(
            productRole.id(),
            account.getTenant().getId(),
            clientId,
            account.getId(),
            productRole.roleProductId(),
            productRole.roleProductName(),
            account.getId(),
            false,
            true,
            true,
            true,
            true,
            true,
            true
        );

        accountService.grantProduct(account.getId(), updatedProductRole);
    }

    // add revoke method as soft delete
    @Transactional
    public void revokeProduct(Long clientId, Long id) {

        UserDetailsImpl currentUser = getCurrentUser();
        AccountEntity account = accountRepository.findClientAccountById(currentUser.getTenantCode(), clientId)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + clientId));

        ProductRole productRole = accountService.getProductRolesByAccountId(account.getId()).stream()
            .filter(role -> Objects.equals(role.id(), id))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Product role not found for id: " + id));

        accountService.revokeProduct(account.getId(), productRole.id());

        }

    
}

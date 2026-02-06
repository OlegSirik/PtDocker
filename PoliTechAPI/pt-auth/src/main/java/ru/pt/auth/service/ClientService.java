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
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AccountService;
import ru.pt.api.service.product.ProductService;
import ru.pt.auth.entity.*;
import ru.pt.auth.model.ClientSecurityConfig;
import ru.pt.auth.security.permitions.AuthZ;
import ru.pt.auth.security.permitions.AuthorizationService;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.ClientRepository;
import ru.pt.auth.security.SecurityContextHelper;
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
    private final AuthorizationService authorizationService;

    public ClientService(
            ClientRepository clientRepository,
            AccountLoginRepository accountLoginRepository,
            AccountRepository accountRepository,
            TenantService tenantService,
            SecurityContextHelper securityContextHelper,
            AccountService accountService,
            ProductService productService,
            AuthorizationService authorizationService) {
        this.clientRepository = clientRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.accountRepository = accountRepository;
        this.tenantService = tenantService;
        this.securityContextHelper = securityContextHelper;
        this.clientMapper = new ClientMapper();
        this.accountService = accountService;
        this.productService = productService;
        this.authorizationService = authorizationService;
    }

    public Optional<ClientEntity> findByClientId(String clientId) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CLIENT, clientId, null, AuthZ.Action.VIEW);
        return clientRepository.findByClientId(clientId);
    }

    /**
     * Получает логин для базового аккаунта клиента
     */
    public Optional<String> getDefaultAccountLogin(Long defaultAccountId) {
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CLIENT, null, defaultAccountId, AuthZ.Action.VIEW);
        return accountLoginRepository.findByAccountId(defaultAccountId)
                .map(AccountLoginEntity::getUserLogin);
    }

    /** Used during auth flow (e.g. AccountResolverService); no auth check - called before user is resolved. */
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
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CLIENT, null, null, AuthZ.Action.MANAGE);

        TenantEntity tenant = tenantService.findByCode(user.getTenantCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Проверка уникальности clientId
        if (clientRepository.findByClientIdandTenantCode(client.getClientId(), user.getTenantCode()).isPresent()) {
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
        AuthenticatedUser user = getCurrentUser();
        ClientEntity clientToUpdate = clientRepository.findById(client.getId())
                .orElseThrow(() -> new NotFoundException("Client not found"));
        AccountEntity clientAccount = accountRepository.findCliensAccountByClientId(clientToUpdate.getId())
                .orElse(null);

        authorizationService.check(user, AuthZ.ResourceType.CLIENT, String.valueOf(client.getId()),
                clientAccount != null ? clientAccount.getId() : null, AuthZ.Action.MANAGE);

        TenantEntity tenant = tenantService.findByCode(user.getTenantCode())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        if ("TNT_ADMIN".equals(user.getUserRole())) {
            // TNT_ADMIN: only clients from own tenant. SYS_ADMIN: any client.
            if (!clientToUpdate.getTenant().getCode().equals(tenant.getCode())) {
                throw new ForbiddenException("You do not have access to this tenant data");
            }
        }

        if (!clientToUpdate.getClientId().equals(client.getClientId())) {
            // Проверка уникальности нового clientId
            ClientEntity oldClient = clientRepository.findByClientIdandTenantCode(client.getClientId(), user.getTenantCode())
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
        AuthenticatedUser user = getCurrentUser();
        authorizationService.check(user, AuthZ.ResourceType.CLIENT, null, null, AuthZ.Action.LIST);

        List<ClientEntity> clients = clientRepository.findBytenantCode(user.getTenantCode());

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
        AuthenticatedUser user = getCurrentUser();
        ClientEntity client = clientRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Client not found"));
        AccountEntity account = accountRepository.findCliensAccountByClientId(id)
                .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + id));
        authorizationService.check(user, AuthZ.ResourceType.CLIENT, String.valueOf(id), account.getId(), AuthZ.Action.VIEW);

        Client clientDto = clientMapper.toDto(client);
        clientDto.setClientAccountId(account.getId());
        return clientDto;
    }

    // ========== HELPER METHODS ==========

    private AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }

    @Transactional(readOnly = true)
    public List<ProductRole> listClientProducts(Long clientId) {
        AuthenticatedUser user = getCurrentUser();
        AccountEntity account = accountRepository.findClientAccountById(user.getTenantCode(), clientId)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + clientId));
        authorizationService.check(user, AuthZ.ResourceType.CLIENT_PRODUCTS, String.valueOf(clientId), account.getId(), AuthZ.Action.LIST);

        return accountService.getProductRolesByAccountId(account.getId());
        //accountService.getProd
    }

    @Transactional
    public void grantProduct(Long clientId, ProductRole productRole) {
        AuthenticatedUser user = getCurrentUser();
        AccountEntity account = accountRepository.findClientAccountById(user.getTenantCode(), clientId)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + clientId));
        authorizationService.check(user, AuthZ.ResourceType.CLIENT_PRODUCTS, String.valueOf(clientId), account.getId(), AuthZ.Action.MANAGE);

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

        AuthenticatedUser currentUser = getCurrentUser();
        AccountEntity account = accountRepository.findClientAccountById(currentUser.getTenantCode(), clientId)
            .orElseThrow(() -> new NotFoundException("Client account not found for client id: " + clientId));

        ProductRole productRole = accountService.getProductRolesByAccountId(account.getId()).stream()
            .filter(role -> Objects.equals(role.id(), id))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("Product role not found for id: " + id));

        accountService.revokeProduct(account.getId(), productRole.id());

        }

    
}

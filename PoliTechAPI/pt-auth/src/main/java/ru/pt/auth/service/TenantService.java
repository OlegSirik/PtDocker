package ru.pt.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.*;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.model.TenantSecurityConfig;
import ru.pt.auth.repository.*;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ru.pt.api.dto.auth.Tenant;
import ru.pt.auth.utils.TenantMapper;
@Service
public class TenantService implements TenantSecurityConfigService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final SecurityContextHelper securityContextHelper;
    private final TenantMapper tenantMapper;
    public TenantService(
            TenantRepository tenantRepository,
            AccountRepository accountRepository,
            ClientRepository clientRepository,
            LoginRepository loginRepository,
            AccountLoginRepository accountLoginRepository,
            SecurityContextHelper securityContextHelper,
            TenantMapper tenantMapper) {
        this.tenantRepository = tenantRepository;
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.loginRepository = loginRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.securityContextHelper = securityContextHelper;
        this.tenantMapper = tenantMapper;
    }

    public Optional<TenantEntity> findByCode(String code) {
        return tenantRepository.findByCode(code.toLowerCase());
    }

    public Optional<TenantEntity> findByName(String name) {
        return tenantRepository.findByName(name.toUpperCase());
    }

    public TenantEntity save(TenantEntity tenantEntity) {
        tenantEntity.setCode(tenantEntity.getCode().toLowerCase());
        tenantEntity.setName(tenantEntity.getName().toUpperCase());
        return tenantRepository.save(tenantEntity);
    }

    public List<TenantEntity> findAll() {
        return tenantRepository.findAll();
    }

    // ========== TENANT MANAGEMENT (SYS_ADMIN only) ==========

    /**
     * SYS_ADMIN: Получить список всех tenants
     */
    public List<Tenant> getTenants() {
        String tenantCode = getCurrentTenant();
        if (tenantCode.equals(TenantEntity.SYS_TENANT_CODE)) {
        return findAll().stream()
            .map(tenantMapper::toDto)
            .collect(Collectors.toList());
        } else {
            return findAll().stream()
                .filter(tenant -> tenant.getCode().equalsIgnoreCase(tenantCode))
                .map(tenantMapper::toDto)
                .collect(Collectors.toList());
        }
    }

    private Long getNextId() {
        return accountRepository.getNextAccountId();
    }
    /**
     * SYS_ADMIN: Создание нового tenant
     */
    @Transactional
    public Tenant createTenant(Tenant tenantDto) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can create tenants");
        }

        // Проверка уникальности кода
        if (findByName(tenantDto.code()).isPresent()) {
            throw new BadRequestException("Tenant with name '" + tenantDto.code() + "' already exists");
        }

        //TODO AuthType
        TenantEntity tenant = TenantEntity.create(tenantDto.code(), tenantDto.name(), "JWT");
        tenant.setId(getNextId());
        TenantEntity savedTenant = save(tenant);

        // Создаем tenant account
        AccountEntity tenantAccount = AccountEntity.tenantAccount(savedTenant);
        AccountEntity savedTenantAccount = accountRepository.save(tenantAccount);

        logger.info("Tenant '{}' created by SYS_ADMIN", tenant.getName());

        ClientEntity client = ClientEntity.defaultForTenant(tenant);
        client.setId(getNextId());
        ClientEntity savedClient = clientRepository.save(client);

        AccountEntity clientAccount = AccountEntity.clientAccount(savedClient, savedTenantAccount);
        AccountEntity savedClientAccount = accountRepository.save(clientAccount);


        if ( tenant.isSystem()) {
            AccountEntity sysAdminAccount = AccountEntity.sysAdminAccount(savedClientAccount);
            sysAdminAccount.setId(getNextId());
            accountRepository.save(sysAdminAccount);
        } else {
            AccountEntity tntAdminAccount = AccountEntity.tntAdminAccount(savedClientAccount);
            tntAdminAccount.setId(getNextId());
            accountRepository.save(tntAdminAccount);
        }

        AccountEntity productAdminAccount = AccountEntity.productAdminAccount(savedClientAccount);
        productAdminAccount.setId(getNextId());
        accountRepository.save(productAdminAccount);

        AccountEntity defaultClientAccount = AccountEntity.defaultClientAccount(savedClientAccount);
        defaultClientAccount.setId(getNextId());
        AccountEntity savedDefaultClientAccount = accountRepository.save(defaultClientAccount);

        savedClient.setDefaultAccountId(savedDefaultClientAccount.getId());
        clientRepository.save(savedClient);
        
        logger.info("Default client '{}' created for tenant '{}'", savedClient.getName(), savedTenant.getName());

        return tenantMapper.toDto(savedTenant);
    }

    /**
     * SYS_ADMIN: Обновление tenant
     */
    @Transactional
    public Tenant updateTenant(Tenant tenantDto) {
        UserDetailsImpl currentUser = getCurrentUser();
        String tenantCode = getCurrentTenant();
        
        if (!("SYS_ADMIN".equals(currentUser.getUserRole()) || "TNT_ADMIN".equals(currentUser.getUserRole()))) {
            throw new ForbiddenException("Only SYS_ADMIN can update tenants");
        }
        if (("TNT_ADMIN".equals(currentUser.getUserRole())) && !tenantCode.equals(tenantDto.code())) {
            throw new ForbiddenException("Only TNT_ADMIN can update tenants");
        }


        TenantEntity tenantEntity = findByCode(tenantDto.code())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        // Пока только название можно поменять
        tenantEntity.setName(tenantDto.name());
        tenantEntity.setAuthType(tenantDto.authType());
        tenantEntity.setDeleted(tenantDto.isDeleted());
        TenantEntity savedTenantEntity = save(tenantEntity);
        return tenantMapper.toDto(savedTenantEntity);
    }

    /**
     * SYS_ADMIN: Удаление tenant (soft delete)
     */
    @Transactional
    public void deleteTenant(String tenantCode) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can delete tenants");
        }

        TenantEntity tenant = findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant not found with ID: " + tenantCode));

        tenant.setDeleted(true);
        save(tenant);
        logger.info("Tenant '{}' deleted by SYS_ADMIN", tenant.getName());
    }

    // ========== HELPER METHODS ==========

    /**
     * Gets the current authenticated user.
     * @return UserDetailsImpl representing the current user
     * @throws ForbiddenException if user is not authenticated
     */
    public UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }

    /**
     * Gets the current tenant entity based on the authenticated user's tenant code.
     * @return TenantEntity representing the current tenant
     * @throws ForbiddenException if user is not authenticated
     * @throws NotFoundException if tenant is not found
     */
    public String getCurrentTenant() {
        UserDetailsImpl currentUser = getCurrentUser();
        String tenantCode = currentUser.getTenantCode();
        return tenantCode;
    }

    @Override
    @Cacheable(
        cacheNames = "tenant-security-config",
        key = "#tenantCode"
    )
    public TenantSecurityConfig getConfig(String tenantCode) {
        TenantEntity entity = tenantRepository
            .findByCode(tenantCode)
            .orElseThrow(() ->
                new NotFoundException("Tenant not found: " + tenantCode)
            );

        return mapToDomain(entity);
    }

    private TenantSecurityConfig mapToDomain(TenantEntity e) {
        return new TenantSecurityConfig(
            e.getCode(),
            AuthType.valueOf(e.getAuthType()),
            null,
            null,
            null,
            null
        );
    }
}

package ru.pt.auth.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.*;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.model.TenantSecurityConfig;
import ru.pt.auth.model.AuthProperties;
import ru.pt.auth.repository.*;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.security.UserDetailsImpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ru.pt.api.dto.auth.Tenant;
import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.dto.refs.TenantAuthType;
import ru.pt.auth.utils.TenantMapper;
import ru.pt.api.service.auth.TenantConfig;
import ru.pt.api.service.db.RefDictAdminService;
import ru.pt.api.service.schema.SchemaService;

@Service
@RequiredArgsConstructor
public class TenantService implements TenantSecurityConfigService, TenantConfig {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final LoginRepository loginRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final SecurityContextHelper securityContextHelper;
    private final TenantMapper tenantMapper;
    private final SchemaService schemaService;
    private final RefDictAdminService refDictAdminService;

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<TenantEntity> findByCode(String code) {
        return tenantRepository.findByCode(code.toLowerCase());
    }

    public Optional<TenantEntity> findByCodeAnyStatus(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return tenantRepository.findByCodeAnyStatus(code.trim());
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
        TenantEntity tenant = TenantEntity.create(tenantDto.code(), tenantDto.name(), "LOCAL_JWT");
        if (tenant.getAuthType() == null ) {
            tenant.setAuthType("LOCAL_JWT");
        }
        if (tenant.getStorageType() == null ) {
            tenant.setStorageType("DB");
        }
        tenant.setId(getNextId());

        TenantEntity savedTenant = save(tenant);

        // Создаем tenant account
        AccountEntity tenantAccount = AccountEntity.tenantAccount(savedTenant);
        // root path is just its own id
        tenantAccount.setIdPath(tenantAccount.getId().toString());
        AccountEntity savedTenantAccount = accountRepository.save(tenantAccount);

        logger.info("Tenant '{}' created by SYS_ADMIN", tenant.getName());

        ClientEntity client = ClientEntity.defaultForTenant(tenant);
        client.setId(getNextId());
        ClientEntity savedClient = clientRepository.save(client);

        AccountEntity clientAccount = AccountEntity.clientAccount(savedClient, savedTenantAccount);
        clientAccount.setIdPath(savedTenantAccount.getIdPath() + "." + clientAccount.getId());
        AccountEntity savedClientAccount = accountRepository.save(clientAccount);


        if ( tenant.isSystem()) {
            AccountEntity sysAdminAccount = AccountEntity.createAccount(savedClientAccount, null, AccountNodeType.SYS_ADMIN);
            sysAdminAccount.setId(getNextId());
            sysAdminAccount.setIdPath(savedClientAccount.getIdPath() + "." + sysAdminAccount.getId());
            accountRepository.save(sysAdminAccount);
        } else {
            AccountEntity tntAdminAccount = AccountEntity.createAccount(savedClientAccount, null, AccountNodeType.TNT_ADMIN);
            tntAdminAccount.setId(getNextId());
            tntAdminAccount.setIdPath(savedClientAccount.getIdPath() + "." + tntAdminAccount.getId());
            accountRepository.save(tntAdminAccount);
        }

        AccountEntity productAdminAccount = AccountEntity.createAccount(savedClientAccount, null, AccountNodeType.PRODUCT_ADMIN);
        productAdminAccount.setId(getNextId());
        productAdminAccount.setIdPath(savedClientAccount.getIdPath() + "." + productAdminAccount.getId());
        accountRepository.save(productAdminAccount);

        AccountEntity defaultClientAccount = AccountEntity.createAccount(savedClientAccount, "Default account for client", AccountNodeType.ACCOUNT);
        defaultClientAccount.setId(getNextId());
        defaultClientAccount.setIdPath(savedClientAccount.getIdPath() + "." + defaultClientAccount.getId());
        AccountEntity savedDefaultClientAccount = accountRepository.save(defaultClientAccount);

        savedClient.setDefaultAccountId(savedDefaultClientAccount.getId());
        clientRepository.save(savedClient);
        
        logger.info("Default client '{}' created for tenant '{}'", savedClient.getName(), savedTenant.getName());

        schemaService.newTenantCreated(savedTenant.getId());
        refDictAdminService.newTenantCreated(savedTenant.getId());

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

        // Include DELETED so soft-deleted tenants can be reactivated
        TenantEntity tenantEntity = findByCodeAnyStatus(tenantDto.code())
                .orElseThrow(() -> new NotFoundException("Tenant not found"));

        String currentStatus = tenantEntity.getRecordStatus() != null
                ? tenantEntity.getRecordStatus().toUpperCase()
                : RecordStatus.ACTIVE.getValue();
        String newStatus = tenantDto.recordStatus() != null
                ? tenantDto.recordStatus().getValue()
                : currentStatus;

        if (RecordStatus.DELETED.getValue().equalsIgnoreCase(currentStatus)) {
            // From DELETED only reactivation to ACTIVE is allowed
            if (!RecordStatus.ACTIVE.getValue().equalsIgnoreCase(newStatus)) {
                throw new BadRequestException(
                        "Tenant in DELETED status can only be changed to ACTIVE");
            }
            tenantEntity.setRecordStatus(RecordStatus.ACTIVE.getValue());
            tenantEntity.setUpdatedAt(LocalDateTime.now());
            logger.info("Tenant '{}' reactivated DELETED → ACTIVE by {}",
                    tenantEntity.getCode(), currentUser.getUsername());
            TenantEntity saved = save(tenantEntity);
            return tenantMapper.toDto(saved);
        }

        tenantEntity.setName(tenantDto.name());
        if (tenantDto.authType() != null) {
            tenantEntity.setAuthType(tenantDto.authType().getValue());
        }
        if (tenantDto.recordStatus() != null) {
            tenantEntity.setRecordStatus(tenantDto.recordStatus().getValue());
        }
        tenantEntity.setUpdatedAt(LocalDateTime.now());
        TenantEntity savedTenantEntity = save(tenantEntity);
        return tenantMapper.toDto(savedTenantEntity);
    }

    /**
     * SYS_ADMIN: soft-delete ACTIVE tenant; hard-purge if already DELETED.
     */
    @Transactional
    public void softDeleteTenant(String tenantCode) {
        UserDetailsImpl currentUser = getCurrentUser();
        if (!"SYS_ADMIN".equals(currentUser.getUserRole())) {
            throw new ForbiddenException("Only SYS_ADMIN can delete tenants");
        }

        TenantEntity tenant = findByCodeAnyStatus(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantCode));

        if (tenant.isSystem() || TenantEntity.SYS_TENANT_CODE.equalsIgnoreCase(tenant.getCode())) {
            throw new BadRequestException("Cannot delete system tenant 'sys'");
        }

        if (RecordStatus.DELETED.getValue().equalsIgnoreCase(tenant.getRecordStatus())) {
            hardPurgeTenant(tenant);
            return;
        }

        tenant.setRecordStatus(RecordStatus.DELETED.getValue());
        tenant.setUpdatedAt(LocalDateTime.now());
        save(tenant);
        logger.info("Tenant '{}' soft-deleted by SYS_ADMIN", tenant.getName());
    }

    /**
     * Hard-delete all tenant-scoped rows then the tenant row itself.
     */
    @Transactional
    public void hardPurgeTenant(TenantEntity tenant) {
        Long tid = tenant.getId();
        String code = tenant.getCode();
        logger.info("Hard-purging tenant id={} code={}", tid, code);

        // Leaf → root (FK-safe). Tables without tid use joins.
        execSql("DELETE FROM po_addon_policies WHERE policy_id IN (SELECT id FROM policy_index WHERE tid = :tid)", tid);
        execSql("DELETE FROM policy_data WHERE tid = :tid", tid);
        execSql("DELETE FROM policy_index WHERE tid = :tid", tid);

        execSql("DELETE FROM pt_payment_allocation WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_payment WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_payment_installment WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_installment_templates WHERE tid = :tid", tid);

        execSql("DELETE FROM coefficient_data WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_calculators WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_lob_calculators WHERE tid = :tid", tid);

        // pt_product_tests has no tid — delete via product_id
        execSql("DELETE FROM pt_product_tests WHERE product_id IN (SELECT id FROM pt_products WHERE tid = :tid)", tid);
        execSql("DELETE FROM pt_product_versions WHERE tid = :tid", tid);
        execSql("DELETE FROM po_addon_products WHERE tid = :tid", tid);
        execSql("DELETE FROM po_pricelists WHERE tid = :tid", tid);
        execSql("DELETE FROM po_providers WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_products WHERE tid = :tid", tid);

        // pt_lob_policy_forms dropped in V17; pt_policy_forms is global catalog (tid=1 only)
        execSql("DELETE FROM pt_policy_forms WHERE tid = :tid AND tid <> 1", tid);
        execSql("DELETE FROM pt_lobs WHERE tid = :tid", tid);

        execSql("DELETE FROM pt_number_generators WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_files WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_currency_rate WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_insurance_company WHERE tid = :tid", tid);

        execSql("DELETE FROM pt_rules WHERE tid = :tid", tid);
        execSql("DELETE FROM llm_exchange WHERE tid = :tid", tid);

        execSql("DELETE FROM pt_refdata WHERE tid = :tid", tid);
        execSql("DELETE FROM pt_refdicts WHERE tid = :tid", tid);
        execSql("DELETE FROM mt_attribute_def WHERE tenant_id = :tid", tid);
        execSql("DELETE FROM mt_contract_model WHERE tid = :tid", tid);

        execSql("DELETE FROM acc_commission_rates WHERE tid = :tid", tid);
        execSql("DELETE FROM acc_product_roles WHERE tid = :tid", tid);
        execSql("DELETE FROM acc_account_tokens WHERE tid = :tid", tid);
        execSql("DELETE FROM acc_account_logins WHERE tid = :tid", tid);

        @SuppressWarnings("unchecked")
        List<Number> cfgIds = entityManager.createNativeQuery(
                        "SELECT client_configuration_entity_id FROM acc_clients WHERE tid = :tid AND client_configuration_entity_id IS NOT NULL")
                .setParameter("tid", tid)
                .getResultList();
        execSql("UPDATE acc_clients SET default_account_id = NULL, client_configuration_entity_id = NULL WHERE tid = :tid", tid);
        execSql("DELETE FROM acc_accounts WHERE tid = :tid", tid);
        for (Number cfgId : cfgIds) {
            if (cfgId == null) continue;
            try {
                entityManager.createNativeQuery("DELETE FROM acc_client_configuration WHERE id = :id")
                        .setParameter("id", cfgId.longValue())
                        .executeUpdate();
            } catch (Exception e) {
                logger.warn("purge client_configuration id={}: {}", cfgId, e.getMessage());
            }
        }
        execSql("DELETE FROM acc_clients WHERE tid = :tid", tid);
        execSql("DELETE FROM acc_logins WHERE tid = :tid", tid);

        entityManager.flush();
        tenantRepository.delete(tenant);
        entityManager.flush();
        logger.info("Tenant id={} code={} hard-purged", tid, code);
    }

    private void execSql(String sql, Long tid) {
        // SAVEPOINT: one bad SQL must not abort the whole PG transaction
        entityManager.createNativeQuery("SAVEPOINT tenant_purge_sp").executeUpdate();
        try {
            int n = entityManager.createNativeQuery(sql)
                    .setParameter("tid", tid)
                    .executeUpdate();
            entityManager.createNativeQuery("RELEASE SAVEPOINT tenant_purge_sp").executeUpdate();
            logger.debug("purge sql rows={} sql={}", n, sql);
        } catch (Exception e) {
            try {
                entityManager.createNativeQuery("ROLLBACK TO SAVEPOINT tenant_purge_sp").executeUpdate();
            } catch (Exception rollbackEx) {
                logger.warn("purge savepoint rollback failed: {}", rollbackEx.getMessage());
            }
            logger.warn("purge skip (tid={}): {} — {}", tid, sql, e.getMessage());
        }
    }

    @Transactional
    public void deleteTenant(Tenant tenantDto) {
        softDeleteTenant(tenantDto.code());
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

    @Override
    public Optional<TenantSecurityConfig> getDeletedTenantConfig(String tenantCode) {
        return findByCodeAnyStatus(tenantCode)
            .filter(e -> RecordStatus.DELETED.getValue().equalsIgnoreCase(e.getRecordStatus()))
            .map(this::mapToDomain);
    }

    private TenantSecurityConfig mapToDomain(TenantEntity e) {
        var authCfg = e.getAuthConfig();
        String issuer = null;
        String jwksUri = null;
        if (authCfg != null && !authCfg.isEmpty()) {
            issuer = authCfg.get(AuthProperties.ISSUER.value());
            jwksUri = authCfg.get(AuthProperties.JWKS_URI.value());
        }
        return new TenantSecurityConfig(
            e.getCode(),
            AuthType.valueOf(e.getAuthType()),
            issuer,
            jwksUri,
            null,
            null
        );
    }

    @Override
    public Tenant getTenant(String tenantCode) {
        TenantEntity entity = tenantRepository.findByCode(tenantCode)
            .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantCode));
        return new Tenant(
            entity.getId(),
            entity.getName(),
            RecordStatus.valueOf(entity.getRecordStatus()),
            TenantAuthType.fromValue(entity.getAuthType()),
            FileStorageType.valueOf(entity.getStorageType()),
            entity.getCode(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getStorageConfig() != null ? entity.getStorageConfig() : new HashMap<>(),
            entity.getAuthConfig() != null ? entity.getAuthConfig() : new HashMap<>()
        );
    }

    @Override
    public Tenant getTenantById(Long tenantId) {
        TenantEntity entity = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId));
        return tenantMapper.toDto(entity);
    }
}

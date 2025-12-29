package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.model.AuthType;
import ru.pt.auth.model.TenantSecurityConfig;
import ru.pt.auth.repository.TenantRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService  implements TenantSecurityConfigService{

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
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


    @Override
    @Cacheable(
        cacheNames = "tenant-security-config",
        key = "#tenantCode"
    )
    public TenantSecurityConfig getConfig(String tenantCode) {

        TenantEntity entity = tenantRepository
            .findByCode(tenantCode)
            .orElseThrow(() ->
                new IllegalArgumentException("Tenant not found: " + tenantCode)
            );

        return mapToDomain(entity);
    }

    private TenantSecurityConfig mapToDomain(TenantEntity e) {
        return new TenantSecurityConfig(
            e.getCode(),
            AuthType.valueOf(e.getAuthType()), //e.getAuthType(),
            null,  //e.getJwtIssuer(),
            null,  //e.getJwtPublicKey(),
            null,  //e.getClientIdHeader(),
            null //e.getUserIdHeader()
        );
    }
}

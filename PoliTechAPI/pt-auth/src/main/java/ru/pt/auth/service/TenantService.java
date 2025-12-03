package ru.pt.auth.service;

import org.springframework.stereotype.Service;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.repository.TenantRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TenantService {
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public Optional<TenantEntity> findByCode(String code) {
        return tenantRepository.findByCode(code.toUpperCase());
    }

    public Optional<TenantEntity> findByName(String name) {
        return tenantRepository.findByName(name.toUpperCase());
    }

    public TenantEntity save(TenantEntity tenantEntity) {
        return tenantRepository.save(tenantEntity);
    }

    public List<TenantEntity> findAll() {
        return tenantRepository.findAll();
    }

}

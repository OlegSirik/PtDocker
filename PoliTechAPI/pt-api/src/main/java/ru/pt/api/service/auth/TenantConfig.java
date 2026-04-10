package ru.pt.api.service.auth;

import ru.pt.api.dto.auth.Tenant;

public interface TenantConfig {

    Tenant getTenant(String tenantCode);
    Tenant getTenantById(Long tenantId);
}

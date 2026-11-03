package ru.pt.auth.service;

import java.util.Optional;

import ru.pt.auth.model.TenantSecurityConfig;

public interface TenantSecurityConfigService {

    /** ACTIVE tenants only. */
    TenantSecurityConfig getConfig(String tenantCode);

    /**
     * Config for a soft-deleted tenant, if present.
     * Caller must allow access only to {@code SYS_ADMIN} after authentication.
     */
    Optional<TenantSecurityConfig> getDeletedTenantConfig(String tenantCode);

}

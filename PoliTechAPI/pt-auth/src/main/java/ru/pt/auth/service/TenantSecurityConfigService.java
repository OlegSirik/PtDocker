package ru.pt.auth.service;

import ru.pt.auth.model.TenantSecurityConfig;

public interface TenantSecurityConfigService {

    TenantSecurityConfig getConfig(String tenantCode);

}

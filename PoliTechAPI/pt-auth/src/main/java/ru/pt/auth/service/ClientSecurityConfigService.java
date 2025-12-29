package ru.pt.auth.service;

import ru.pt.auth.model.ClientSecurityConfig;

public interface ClientSecurityConfigService {
    
    ClientSecurityConfig getConfig(String tenantCode, String authClientId);

}

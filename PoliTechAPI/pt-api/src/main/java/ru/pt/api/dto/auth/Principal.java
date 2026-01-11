package ru.pt.api.dto.auth;

import java.util.Set;
import java.util.List;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

public record Principal(
    Long id,
    String username,
    String tenantCode,
    String accountName,
    Long clientId,
    String clientName,
    String userRole,
    Set<String> productRoles,
    Collection<? extends GrantedAuthority> authorities,
    Boolean isDefault,
    Long accountId,
    List<PrincipalAccount> accounts
) {}

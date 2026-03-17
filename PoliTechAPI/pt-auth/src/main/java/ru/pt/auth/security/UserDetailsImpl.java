package ru.pt.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.AccountTokenEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация UserDetails для Spring Security и AuthenticatedUser для бизнес-логики.
 * Содержит информацию о пользователе, его аккаунте и ролях продуктов.
 */
public class UserDetailsImpl implements UserDetails, AuthenticatedUser {
    /*
    principal (accountId) — WHO
    acting account — WHERE
    permission — WHAT
    resource — ON WHAT
    */

    private final Long id;
    private final String username;
    private final String tenantCode;
    private final Long tenantId;
    private final Long accountId;
    private final String accountName;
    private final String accountPath;
    private final Long clientId;
    private final String clientName;
    private final String userRole;
    private final Set<String> productRoles;
    private final boolean isDefault;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private String impersonatedTenantCode;  
    private Long actingAccountId;  // вершина для дерева доступа, отличается от Id листа account роли

    public UserDetailsImpl(Long id, String username, String tenantCode, Long tenantId,
                          Long accountId, String accountName, Long clientId, String clientName,
                          String userRole, Set<String> productRoles, boolean isDefault, Long actingAccountId,
                            String accountPath) {
        this.id = id;
        this.username = username;
        this.tenantCode = tenantCode;
        this.tenantId = tenantId;
        this.accountId = accountId;
        this.accountName = accountName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.userRole = userRole;
        this.productRoles = productRoles != null ? productRoles : new HashSet<>();
        this.isDefault = isDefault;
        this.enabled = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.actingAccountId = actingAccountId;
        this.accountPath = accountPath; 
    }

    public static UserDetailsImpl build(AccountLoginEntity accountLoginEntity, Set<String> productRoles, AccountEntity actingAccountEntity) {
        /*Long id, String username, String tenantCode, Long tenantId,
                          Long accountId, String accountName, Long clientId, String clientName,
                          String userRole, Set<String> productRoles, boolean isDefault */
        return new UserDetailsImpl(
            accountLoginEntity.getId(),   //Long id
            accountLoginEntity.getUserLogin(), //String username
            accountLoginEntity.getTenant().getCode(), //String tenantCode
            accountLoginEntity.getTenant().getId(), //Long tenantId
            accountLoginEntity.getAccount().getId(), //Long accountId
            accountLoginEntity.getAccount().getName(), //String accountName
            accountLoginEntity.getClient().getId(), //Long clientId
            accountLoginEntity.getClient().getName(), //String clientName
            accountLoginEntity.getAccount().getNodeType().getValue(), //String userRole
            productRoles, //Set<String> productRoles
            accountLoginEntity.getDefault() //boolean isDefault
            , actingAccountEntity.getId()
            , actingAccountEntity.getIdPath()
        );
    }

    public static UserDetailsImpl build(AccountTokenEntity tokenEntity, AccountEntity accountEntity, Set<String> productRoles, AccountEntity actingAccountEntity) {
        return new UserDetailsImpl(
            tokenEntity.getId(),
            tokenEntity.getToken(),
            accountEntity.getTenant().getCode(),
            accountEntity.getTenant().getId(),
            accountEntity.getId(),
            accountEntity.getName(),
            accountEntity.getClient().getId(),
            accountEntity.getClient().getName(),
            accountEntity.getNodeType().getValue(),
            productRoles,
            true,
            actingAccountEntity.getId(),
            actingAccountEntity.getIdPath()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Добавляем основную роль пользователя
        if (userRole != null && !userRole.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole));
        }

        // Добавляем роли продуктов
        authorities.addAll(productRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet()));

        return authorities;
    }

    @Override
    public String getPassword() {
        return ""; // JWT авторизация, пароль не используется
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // ===== AuthenticatedUser interface implementation =====

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getTenantCode() {
        return tenantCode;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public Long getAccountId() {
        return accountId;
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    @Override
    public Long getClientId() {
        return clientId;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public String getUserRole() {
        return userRole;
    }

    @Override
    public Set<String> getProductRoles() {
        return productRoles;
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public Long getActingAccountId() {
        return this.actingAccountId;
    }

    public void setActingAccountId(Long actingAccountId) {
        this.actingAccountId = actingAccountId;
    }

    @Override
    public boolean hasProductRole(String productRole) {
        return productRoles.stream()
                .anyMatch(role -> role.contains(productRole));
    }

    @Override
    public boolean canPerformAction(String productCode, String action) {
        return productRoles.contains(productCode + "_" + action);
    }

    // ===== Additional methods (not in AuthenticatedUser) =====

    public String getImpersonatedTenantCode() {
        return impersonatedTenantCode;
    }

    public void setImpersonatedTenantCode(String impersonatedTenantCode) {
        this.impersonatedTenantCode = impersonatedTenantCode;
    }

    public String getAccountPath() {
        return this.accountPath;
    }
}


package ru.pt.auth.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.pt.auth.entity.AccountLoginEntity;
import ru.pt.auth.entity.LoginEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация UserDetails для Spring Security.
 * Содержит информацию о пользователе, его аккаунте и ролях продуктов.
 */
public class UserDetailsImpl implements UserDetails {
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
    private final Long actingAccountId;  // вершина для дерева доступа, отличается от Id листа account роли

    public UserDetailsImpl(Long id, String username, String tenantCode, Long tenantId,
                          Long accountId, String accountName, Long clientId, String clientName,
                          String userRole, Set<String> productRoles, boolean isDefault, Long actingAccountId) {
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
    }

    /**
     * Создает UserDetailsImpl из LoginEntity и AccountLoginEntity
     *
    public static UserDetailsImpl build(LoginEntity loginEntity, AccountLoginEntity accountLoginEntity,
                                        Set<String> productRoles) {
        return new UserDetailsImpl(
                accountLoginEntity.getId(),
                loginEntity.getUserLogin(),
                loginEntity.getTenant().getCode(),
                loginEntity.getTenant().getId(),
                accountLoginEntity.getAccount().getId(),
                accountLoginEntity.getAccount().getName(),
                accountLoginEntity.getClient().getId(),
                accountLoginEntity.getClient().getName(),
                accountLoginEntity.getAccount().getNodeType().getValue(),
                productRoles,
                accountLoginEntity.getDefault(),
                accountLoginEntity.getAccount().getParent().getId()
        );
    }
    */

    public static UserDetailsImpl build(AccountLoginEntity accountLoginEntity, Set<String> productRoles, Long actingAccountId) {
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
            , actingAccountId
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

    // Дополнительные геттеры для бизнес-логики

    public Long getId() {
        return id;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getUserRole() {
        return userRole;
    }

    public Set<String> getProductRoles() {
        return productRoles;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getImpersonatedTenantCode() {
        return impersonatedTenantCode;
    }

    public void setImpersonatedTenantCode(String impersonatedTenantCode) {
        this.impersonatedTenantCode = impersonatedTenantCode;
    }

    public Long getActingAccountId() {
        return this.actingAccountId;
    }

    /**
     * Проверяет, есть ли у пользователя определенная роль продукта
     */
    public boolean hasProductRole(String productRole) {
        return productRoles.stream()
                .anyMatch(role -> role.contains(productRole));
    }

    /**
     * Проверяет, может ли пользователь выполнять операцию над продуктом
     */
    public boolean canPerformAction(String productCode, String action) {
        return productRoles.contains(productCode + "_" + action);
    }
}


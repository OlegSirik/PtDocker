package ru.pt.auth.security.context;

public interface RequestContext {
    String getTenant();
    String getClient();
    String getLogin();
    Long getAccount();

    // lifecycle
    boolean isTenantResolved();
    boolean isAccountResolved();

    void setTenant(String tenant);
    void setClient(String client);
    void setLogin(String login);
    void setAccount(Long account);

    /** Path tenant is DELETED — only SYS_ADMIN may proceed after auth. */
    boolean isDeletedTenantAccess();
    void setDeletedTenantAccess(boolean deletedTenantAccess);

    void clear();
}

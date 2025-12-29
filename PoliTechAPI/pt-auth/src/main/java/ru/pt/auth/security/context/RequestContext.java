package ru.pt.auth.security.context;

public interface RequestContext {
    String getTenant();
    String getClient();
    String getLogin();
    String getAccount();

    // lifecycle
    boolean isTenantResolved();
    boolean isAccountResolved();

    void setTenant(String tenant);
    void setClient(String client);
    void setLogin(String login);
    void setAccount(String account);

    void clear();
}

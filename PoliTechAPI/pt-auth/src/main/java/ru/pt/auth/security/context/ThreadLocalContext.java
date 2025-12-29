package ru.pt.auth.security.context;

import org.springframework.stereotype.Component;

@Component
public class ThreadLocalContext implements RequestContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_CLIENT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_LOGIN = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_ACCOUNT = new ThreadLocal<>();

    @Override public String getTenant() { return CURRENT_TENANT.get(); }
    @Override public String getClient() { return CURRENT_CLIENT.get(); }
    @Override public String getLogin() { return CURRENT_LOGIN.get(); }
    @Override public String getAccount() { return CURRENT_ACCOUNT.get(); }

    @Override public void setTenant(String tenant) { CURRENT_TENANT.set(tenant); }
    @Override public void setClient(String client) { CURRENT_CLIENT.set(client); }
    @Override public void setLogin(String login) { CURRENT_LOGIN.set(login); }
    @Override public void setAccount(String account) { CURRENT_ACCOUNT.set(account); }

    @Override public boolean isTenantResolved() {
        return CURRENT_TENANT.get() != null;
    }

    @Override public boolean isAccountResolved() {
        return CURRENT_ACCOUNT.get() != null;
    }

    
    @Override
    public void clear() {
        CURRENT_TENANT.remove();
        CURRENT_CLIENT.remove();
        CURRENT_LOGIN.remove();
        CURRENT_ACCOUNT.remove();
    }
}


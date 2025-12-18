package ru.pt.api.admin.dto;

/**
 * DTO для установки/обновления пароля
 * POST /api/auth/set-password
 */
public class SetPasswordRequest {
    private String userLogin;
    private String password;
    private String clientId;
    private String tenantCode;

    // Getters and Setters
    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }
}

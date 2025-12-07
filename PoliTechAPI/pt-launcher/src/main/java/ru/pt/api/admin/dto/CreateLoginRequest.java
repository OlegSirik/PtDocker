package ru.pt.api.admin.dto;

/**
 * DTO для создания пользователя (логина)
 * POST /tnts/{tenantCode}/logins
 */
public class CreateLoginRequest {
    private String tenantCode;
    private String userLogin;
    private String fullName;
    private String position;

    // Getters and Setters
    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}


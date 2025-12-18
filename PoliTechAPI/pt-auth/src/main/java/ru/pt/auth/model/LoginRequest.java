package ru.pt.auth.model;

/**
 * Запрос на авторизацию с логином и паролем
 */
public class LoginRequest {
    private String userLogin;
    private String password;
    private String clientId; 
    // опционально, для выбора конкретного аккаунта

    public LoginRequest() {
    }

    public LoginRequest(String userLogin, String password) {
        this.userLogin = userLogin;
        this.password = password;
    }

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
}


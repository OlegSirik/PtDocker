package ru.pt.auth.model;

/**
 * Запрос на генерацию токена
 */
public class TokenRequest {
    private String userLogin;
    private String clientId; // может быть null

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}

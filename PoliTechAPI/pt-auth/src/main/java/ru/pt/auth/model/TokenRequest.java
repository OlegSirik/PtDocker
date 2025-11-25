package ru.pt.auth.model;

/**
 * Запрос на генерацию токена
 */
public class TokenRequest {
    private String userLogin;
    private Long clientId; // может быть null

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}

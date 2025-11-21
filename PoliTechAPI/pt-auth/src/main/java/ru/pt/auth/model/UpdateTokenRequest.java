package ru.pt.auth.model;

/**
 * DTO для обновления токена
 */
public class UpdateTokenRequest {
    private String userLogin;
    private Long clientId;
    private String newToken;

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

    public String getNewToken() {
        return newToken;
    }

    public void setNewToken(String newToken) {
        this.newToken = newToken;
    }
}

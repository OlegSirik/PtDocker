package ru.pt.api.admin.dto;

/**
 * DTO для ответа с информацией о логине
 */
public class LoginResponse {
    private String id;
    private String userLogin;
    private String fullName;
    private String position;
    private Boolean isDeleted;
    private String createdAt;
    private String updatedAt;

    // Constructors
    public LoginResponse() {}

    public LoginResponse(String id, String userLogin, String fullName, String position) {
        this.id = id;
        this.userLogin = userLogin;
        this.fullName = fullName;
        this.position = position;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}


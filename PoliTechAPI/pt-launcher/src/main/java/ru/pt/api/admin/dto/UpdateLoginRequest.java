package ru.pt.api.admin.dto;

/**
 * DTO для обновления пользователя (логина)
 * PATCH /tnts/{tenantCode}/logins/{id}
 */
public class UpdateLoginRequest {
    private String fullName;
    private String position;
    private Boolean isDeleted;

    // Getters and Setters
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
}


package ru.pt.api.admin.dto;

/**
 * DTO для обновления пользователя (логина)
 * PATCH /tnts/{tenantCode}/logins/{id}
 */
public class UpdateLoginRequest {
    private String tenantCode;
    private String fullName;
    private String position;
    private String recordStatus;

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

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }
}


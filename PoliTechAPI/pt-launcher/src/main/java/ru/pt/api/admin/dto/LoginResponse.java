package ru.pt.api.admin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для ответа с информацией о логине
 */
@Getter
@Setter
public class LoginResponse {
    private String id;
    private String userLogin;
    private String tenantCode;
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
}


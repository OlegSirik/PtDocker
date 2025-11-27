package ru.pt.api.admin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для создания пользователя (логина)
 * POST /tnts/{tenantCode}/logins
 */
@Getter
@Setter
public class CreateLoginRequest {
    private String userLogin;
    private String fullName;
    private String position;
}


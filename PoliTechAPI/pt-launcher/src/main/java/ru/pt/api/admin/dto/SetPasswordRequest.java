package ru.pt.api.admin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для установки/обновления пароля
 * POST /api/auth/set-password
 */
@Getter
@Setter
public class SetPasswordRequest {
    private String userLogin;
    private String password;
    private String clientId;
}


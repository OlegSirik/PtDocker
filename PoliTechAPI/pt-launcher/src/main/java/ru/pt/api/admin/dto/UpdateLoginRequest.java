package ru.pt.api.admin.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для обновления пользователя (логина)
 * PATCH /tnts/{tenantCode}/logins/{id}
 */
@Getter
@Setter
public class UpdateLoginRequest {
    private String fullName;
    private String position;
    private Boolean isDeleted;
}

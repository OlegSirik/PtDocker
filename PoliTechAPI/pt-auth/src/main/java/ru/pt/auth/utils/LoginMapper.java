package ru.pt.auth.utils;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.auth.Login;
import ru.pt.auth.entity.LoginEntity;

@Component
public class LoginMapper {

    public LoginEntity toEntity(Login dto) {
        if (dto == null) {
            return null;
        }

        LoginEntity entity = new LoginEntity();
        entity.setId(dto.getId());

        if (dto.getTid() != null) {
            entity.setTid(dto.getTid());
        }

        entity.setUserLogin(dto.getUserLogin());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    public Login toDto(LoginEntity entity) {
        if (entity == null) {
            return null;
        }

        Login dto = new Login();
        dto.setId(entity.getId());
        dto.setTid(entity.getTid() != null ? entity.getTid() : null);
        dto.setUserLogin(entity.getUserLogin());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

}
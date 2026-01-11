package ru.pt.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.security.JwtTokenUtil;

import java.util.Optional;

/**
 * Сервис для простой аутентификации без Keycloak
 * Проверяет логин/пароль и генерирует JWT токен
 */
@Service
public class SimpleAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleAuthService.class);

    private final LoginRepository loginRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;

    public SimpleAuthService(LoginRepository loginRepository,
                           JwtTokenUtil jwtTokenUtil) {
        this.loginRepository = loginRepository;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Аутентификация пользователя по логину и паролю
     *
     * @param tenantCode код тенанта
     * @param userLogin логин пользователя
     * @param password пароль
     * @param clientId ID клиента это CLIENT_ID для аутентификации от IdP
     * @return JWT токен или null если аутентификация не удалась
     */
    @Transactional(readOnly = true)
    public String authenticate(String tenantCode, String userLogin, String password, String authClientId) {
        try {
            // Найти пользователя
            Optional<LoginEntity> loginEntityOpt = loginRepository.findByTenantCodeAndUserLogin(tenantCode,userLogin);

            if (loginEntityOpt.isEmpty()) {
                logger.warn("User not found: {}", userLogin);
                return null;
            }

            LoginEntity loginEntity = loginEntityOpt.get();

            // Проверить пароль
            if (loginEntity.getPassword() == null || loginEntity.getPassword().isEmpty()) {
                logger.warn("User {} has no password set (Keycloak only)", userLogin);
                return null;
            }

            if (!passwordEncoder.matches(password, loginEntity.getPassword())) {
                logger.warn("Invalid password for user: {}", userLogin);
                return null;
            }

            // Пароль верный, создать токен
            String token = jwtTokenUtil.createToken(tenantCode, authClientId, userLogin);

            if (token == null) {
                logger.error("Failed to create token for user: {}", userLogin);
                return null;
            }

            logger.info("User {} successfully authenticated", userLogin);
            return token;

        } catch (Exception e) {
            logger.error("Authentication error for user: {}", userLogin, e);
            return null;
        }
    }
}


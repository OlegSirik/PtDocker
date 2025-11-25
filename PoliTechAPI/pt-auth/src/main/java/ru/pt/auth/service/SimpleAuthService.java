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
     * @param userLogin логин пользователя
     * @param password пароль
     * @param clientId ID клиента (опционально)
     * @return JWT токен или null если аутентификация не удалась
     */
    @Transactional(readOnly = true)
    public String authenticate(String userLogin, String password, Long clientId) {
        try {
            // Найти пользователя
            Optional<LoginEntity> loginEntityOpt = loginRepository.findByUserLogin(userLogin);

            if (loginEntityOpt.isEmpty()) {
                logger.warn("User not found: {}", userLogin);
                return null;
            }

            LoginEntity loginEntity = loginEntityOpt.get();

            // Проверить пароль
            if (loginEntity.getPasswordHash() == null || loginEntity.getPasswordHash().isEmpty()) {
                logger.warn("User {} has no password set (Keycloak only)", userLogin);
                return null;
            }

            if (!passwordEncoder.matches(password, loginEntity.getPasswordHash())) {
                logger.warn("Invalid password for user: {}", userLogin);
                return null;
            }

            // Пароль верный, создать токен
            String token = jwtTokenUtil.createToken(userLogin, clientId);

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

    /**
     * Установить пароль для пользователя (только хэш)
     *
     * @param userLogin логин пользователя
     * @param password пароль в открытом виде
     * @return true если пароль успешно установлен
     */
    @Transactional
    public boolean setPassword(String userLogin, String password) {
        try {
            Optional<LoginEntity> loginEntityOpt = loginRepository.findByUserLogin(userLogin);

            if (loginEntityOpt.isEmpty()) {
                logger.warn("User not found: {}", userLogin);
                return false;
            }

            LoginEntity loginEntity = loginEntityOpt.get();
            String hash = passwordEncoder.encode(password);
            loginEntity.setPasswordHash(hash);
            loginRepository.save(loginEntity);

            logger.info("Password set for user: {}", userLogin);
            return true;

        } catch (Exception e) {
            logger.error("Failed to set password for user: {}", userLogin, e);
            return false;
        }
    }

    /**
     * Проверить, есть ли у пользователя установленный пароль
     *
     * @param userLogin логин пользователя
     * @return true если пароль установлен
     */
    @Transactional(readOnly = true)
    public boolean hasPassword(String userLogin) {
        Optional<LoginEntity> loginEntityOpt = loginRepository.findByUserLogin(userLogin);
        return loginEntityOpt.map(loginEntity ->
            loginEntity.getPasswordHash() != null && !loginEntity.getPasswordHash().isEmpty()
        ).orElse(false);
    }
}


package ru.pt.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.repository.TenantRepository;

import java.util.List;

/**
 * Сервис для работы с логинами пользователей согласно документации acc_logins.md
 */
@Service
@Transactional
public class LoginManagementService {

    private static final Logger logger = LoggerFactory.getLogger(LoginManagementService.class);

    private final LoginRepository loginRepository;
    private final TenantRepository tenantRepository;
    private final AccountLoginRepository accountLoginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginManagementService(LoginRepository loginRepository,
                                 TenantRepository tenantRepository,
                                 AccountLoginRepository accountLoginRepository
    ) {
        this.loginRepository = loginRepository;
        this.tenantRepository = tenantRepository;
        this.accountLoginRepository = accountLoginRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Создание пользователя (логина)
     * POST /tnts/{tenantCode}/logins
     */
    public LoginEntity createLogin(String tenantCode, String userLogin, String fullName, String position) {
        // Шаг 1: Проверка обязательных параметров
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new BadRequestException("tenantCode is required");
        }
        if (userLogin == null || userLogin.isBlank()) {
            throw new BadRequestException("userLogin is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new BadRequestException("fullName is required");
        }

        // Шаг 2: Проверка наличия тенанта по tenantCode
        TenantEntity tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tenantCode + "' not found"));

        // Шаг 3: Проверка уникальности пользователя для данного тенанта
        if (loginRepository.existsByTenantCodeAndUserLogin(tenant.getCode(), userLogin)) {
            throw new BadRequestException("User with login '" + userLogin + "' already exists for tenant '" + tenantCode + "'");
        }

        // Шаг 4: Создание записи в таблице acc_logins
        LoginEntity login = new LoginEntity();
        login.setTenant(tenant);
        login.setUserLogin(userLogin);
        login.setFullName(fullName);
        login.setPosition(position);
        login.setIsDeleted(false);

        LoginEntity savedLogin = loginRepository.save(login);

        logger.info("Created login for user '{}' in tenant '{}'", userLogin, tenantCode);

        return savedLogin;
    }

    /**
     * Установка/обновление пароля для пользователя
     * POST /api/auth/set-password
     * Требуется роль SYS_ADMIN
     */
    public void setPassword(String userLogin, String password, String clientId) {
        // Шаг 1: Проверка обязательных параметров
        if (userLogin == null || userLogin.isBlank()) {
            throw new BadRequestException("userLogin is required");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("password is required");
        }
        if (clientId == null || clientId.isBlank()) {
            throw new BadRequestException("clientId is required");
        }

        // Шаг 2: Проверка наличия пользователя в таблице acc_logins
        LoginEntity login = loginRepository.findByUserLogin(userLogin)
                .orElseThrow(() -> new NotFoundException("User with login '" + userLogin + "' not found"));

        // Шаг 3: Проверка наличия пользователя для данного клиента
        Long clientIdLong;
        try {
            clientIdLong = Long.parseLong(clientId);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid clientId format");
        }

        boolean hasClientAccess = accountLoginRepository.existsByUserLoginAndClientId(userLogin, clientIdLong);
        if (!hasClientAccess) {
            throw new NotFoundException("User '" + userLogin + "' is not associated with client ID " + clientId);
        }

        // Шаг 4: Хэшировать и установить пароль
        String hashedPassword = passwordEncoder.encode(password);
        login.setPassword(hashedPassword);
        loginRepository.save(login);

        logger.info("Password set for user '{}'", userLogin);
    }

    /**
     * Обновление данных пользователя
     * PATCH /tnts/{tenantCode}/logins/{id}
     */
    public LoginEntity updateLogin(String tenantCode, Long id, String fullName, String position, Boolean isDeleted) {
        // Шаг 1: Проверка наличия тенанта
        TenantEntity tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tenantCode + "' not found"));

        // Шаг 2: Проверка наличия пользователя
        if (id == null) {
            throw new BadRequestException("User id is required");
        }

        // Шаг 3: Проверка наличия пользователя для данного тенанта
        LoginEntity login = loginRepository.findByIdAndTenantCode(id, tenant.getCode())
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found for tenant '" + tenantCode + "'"));

        // Шаг 4: Обновление данных
        boolean updated = false;
        if (fullName != null && !fullName.isBlank()) {
            login.setFullName(fullName);
            updated = true;
        }
        if (position != null) {
            login.setPosition(position);
            updated = true;
        }
        if (isDeleted != null) {
            login.setIsDeleted(isDeleted);
            updated = true;
        }

        if (updated) {
            login = loginRepository.save(login);
            logger.info("Updated login for user '{}' in tenant '{}'", login.getUserLogin(), tenantCode);
        }

        return login;
    }

    /**
     * Получение всех пользователей тенанта
     * GET /tnts/{tenantCode}/logins
     */
    @Transactional(readOnly = true)
    public List<LoginEntity> getLoginsByTenant(String tenantCode) {
        // Шаг 1: Проверка наличия тенанта
        TenantEntity tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tenantCode + "' not found"));

        // Шаг 2: Получение всех логинов для тенанта
        List<LoginEntity> logins = loginRepository.findByTenantCode(tenant.getCode());

        if (logins.isEmpty()) {
            logger.warn("No logins found for tenant '{}'", tenantCode);
        }

        return logins;
    }

    /**
     * Удаление пользователя (soft delete)
     * DELETE /tnts/{tenantCode}/logins/{id}
     */
    public LoginEntity deleteLogin(String tenantCode, Long id) {
        // Шаг 1: Проверка наличия тенанта
        TenantEntity tenant = tenantRepository.findByCode(tenantCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tenantCode + "' not found"));

        // Шаг 2: Проверка наличия пользователя
        if (id == null) {
            throw new BadRequestException("User id is required");
        }

        // Шаг 3: Проверка наличия пользователя для данного тенанта
        LoginEntity login = loginRepository.findByIdAndTenantCode(id, tenant.getCode())
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found for tenant '" + tenantCode + "'"));

        // Шаг 4: Установка флага удаления
        login.setIsDeleted(true);
        login = loginRepository.save(login);

        logger.info("Deleted (soft) login for user '{}' in tenant '{}'", login.getUserLogin(), tenantCode);

        return login;
    }
}


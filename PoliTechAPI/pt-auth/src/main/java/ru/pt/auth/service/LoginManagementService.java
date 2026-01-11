package ru.pt.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.auth.entity.LoginEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.repository.LoginRepository;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.model.LoginDto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с логинами пользователей согласно документации acc_logins.md
 */
@Service
@Transactional
public class LoginManagementService {

    private static final Logger logger = LoggerFactory.getLogger(LoginManagementService.class);

    private final LoginRepository loginRepository;
    private final TenantService tenantService;
    private final AccountLoginRepository accountLoginRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextHelper securityContextHelper;

    public LoginManagementService(LoginRepository loginRepository,
                                 TenantService tenantService,
                                 AccountLoginRepository accountLoginRepository,
                                 SecurityContextHelper securityContextHelper
    ) {
        this.loginRepository = loginRepository;
        this.tenantService = tenantService;
        this.accountLoginRepository = accountLoginRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.securityContextHelper = securityContextHelper;
    }

    /**
     * Создание пользователя (логина)
     * POST /tnts/{tenantCode}/logins
     */
    public LoginDto createLogin(String tenantCode, String userLogin, String fullName, String position) {
        String tntCode = checkPermitionAndGetTenantCode(tenantCode);

        // Шаг 1: Проверка обязательных параметров
        if (tntCode == null || tntCode.isBlank()) {
            throw new BadRequestException("tenantCode is required");
        }
        if (userLogin == null || userLogin.isBlank()) {
            throw new BadRequestException("userLogin is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new BadRequestException("fullName is required");
        }
        
        // Шаг 2: Проверка наличия тенанта по tenantCode
        TenantEntity tenant = tenantService.findByCode(tntCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tntCode + "' not found"));

        // Шаг 3: Проверка уникальности пользователя для данного тенанта
        if (loginRepository.existsByTenantCodeAndUserLogin(tenant.getCode(), userLogin)) {
            throw new BadRequestException("User with login '" + userLogin + "' already exists for tenant '" + tntCode + "'");
        }

        // Шаг 4: Создание записи в таблице acc_logins
        LoginEntity login = new LoginEntity();
        login.setTenant(tenant);
        login.setUserLogin(userLogin);
        login.setFullName(fullName);
        login.setPosition(position);
        login.setIsDeleted(false);

        LoginEntity savedLogin = loginRepository.save(login);

        logger.info("Created login for user '{}' in tenant '{}'", userLogin, tntCode);

        return fromEntity(savedLogin);
    }

    /**
     * Установка/обновление пароля для пользователя
     * POST /api/auth/set-password
     * Требуется роль SYS_ADMIN
     */
    public void setPassword(String tenantCode, String userLogin, String password) {
        String tntCode = checkPermitionAndGetTenantCode(tenantCode);
        // Шаг 1: Проверка обязательных параметров
        if (userLogin == null || userLogin.isBlank()) {
            throw new BadRequestException("userLogin is required");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("password is required");
        }

        // Шаг 2: Проверка наличия пользователя в таблице acc_logins
        LoginEntity login = loginRepository.findByTenantCodeAndUserLogin(tntCode, userLogin)
                .orElseThrow(() -> new NotFoundException("User with login '" + userLogin + "' not found"));


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
    public LoginDto updateLogin(String tenantCode, Long id, String fullName, String position, Boolean isDeleted) {
        String tntCode = checkPermitionAndGetTenantCode(tenantCode);
        // Шаг 1: Проверка наличия тенанта
        TenantEntity tenant = tenantService.findByCode(tntCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tntCode + "' not found"));

        // Шаг 2: Проверка наличия пользователя
        if (id == null) {
            throw new BadRequestException("User id is required");
        }

        // Шаг 3: Проверка наличия пользователя для данного тенанта
        LoginEntity login = loginRepository.findByIdAndTenantCode(id, tenant.getCode())
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found for tenant '" + tntCode + "'"));
        
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
            logger.info("Updated login for user '{}' in tenant '{}'", login.getUserLogin(), tntCode);
        }

        return fromEntity(login);
    }

    /**
     * Получение всех пользователей тенанта
     * GET /tnts/{tenantCode}/logins
     */
    @Transactional(readOnly = true)
    public List<LoginDto> getLoginsByTenant(String tenantCode) {
        String tntCode = checkPermitionAndGetTenantCode(tenantCode);
        // Шаг 1: Проверка наличия тенанта
        List<LoginEntity> logins = new ArrayList<LoginEntity>();
        try {
        TenantEntity tenant = tenantService.findByCode(tntCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tntCode + "' not found"));

        // Шаг 2: Получение всех логинов для тенанта
        
            logins = loginRepository.findByTenantCode(tenant.getCode());
        } catch (NotFoundException e) {}

        if (logins.isEmpty()) {
            logger.warn("No logins found for tenant '{}'", tntCode);
        }

        return logins.stream().map(this::fromEntity).collect(Collectors.toList());
    }

    /**
     * Удаление пользователя (soft delete)
     * DELETE /tnts/{tenantCode}/logins/{id}
     */
    public void deleteLogin(String tenantCode, Long id) {
        String tntCode = checkPermitionAndGetTenantCode(tenantCode);
        // Шаг 1: Проверка наличия тенанта
        TenantEntity tenant = tenantService.findByCode(tntCode)
                .orElseThrow(() -> new NotFoundException("Tenant with code '" + tntCode + "' not found"));

        // Шаг 2: Проверка наличия пользователя
        if (id == null) {
            throw new BadRequestException("User id is required");
        }

        // Шаг 3: Проверка наличия пользователя для данного тенанта
        LoginEntity login = loginRepository.findByIdAndTenantCode(id, tenant.getCode())
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found for tenant '" + tntCode + "'"));

        // Шаг 4: Установка флага удаления
        login.setIsDeleted(true);
        login = loginRepository.save(login);

        logger.info("Deleted (soft) login for user '{}' in tenant '{}'", login.getUserLogin(), tntCode);

    }



    // HELPERS 
    private UserDetailsImpl getCurrentUser() {
        return securityContextHelper.getCurrentUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }
    
    private boolean userIsSysAdmin() {
        UserDetailsImpl currentUser = getCurrentUser();
        return UserRole.SYS_ADMIN.getValue().equals(currentUser.getUserRole());
    }

    private String checkPermitionAndGetTenantCode(String tenantCode) {

        if ( userIsSysAdmin() )
        {
            String currentTenantCode = getCurrentUser().getImpersonatedTenantCode();
            if (currentTenantCode == null) {
                throw new ForbiddenException("SYS ADMIN must be impersonated");
            }
            return currentTenantCode;
        }
        if ( !tenantCode.equals( getCurrentUser().getTenantCode() )) {
            throw new ForbiddenException("Only SYS_ADMIN or TNT_ADMIN can access other tenants");
        }
        return tenantCode;
    }

    private LoginDto fromEntity(LoginEntity login) {
        
        return new LoginDto(
            Long.valueOf(login.getId()),
            null,
            "",
            login.getUserLogin(),
            login.getFullName(),
            login.getPosition()
        );
    }
}


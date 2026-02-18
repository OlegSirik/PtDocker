package ru.pt.config;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.service.product.LobService;
import ru.pt.auth.entity.*;
import ru.pt.auth.entity.AccountNodeType;
import ru.pt.auth.repository.AccountLoginRepository;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.auth.service.TenantService;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import ru.pt.api.dto.auth.Tenant;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import ru.pt.auth.repository.AccountRepository;
/**
 * Application initializer that runs after the Spring application context is fully loaded.
 * Use this class to perform initial setup tasks such as:
 * - Data initialization
 * - Default tenant/client creation
 * - System configuration validation
 * - Cache warming
 * 
 * The @Order annotation can be used to control execution order if multiple ApplicationRunner beans exist.
 * Lower values have higher priority.
 */
@Component
@Order(1) // Execute first among ApplicationRunners (if multiple exist)
public class ApplicationInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    private final AccountLoginRepository accountLoginRepository;
    private final TenantService tenantService;
    private final LobService lobService;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;
    private final AccountRepository accountRepository;
    public ApplicationInitializer(TenantService tenantService,
                                  AccountLoginRepository accountLoginRepository,
                                  LobService lobService,
                                  ObjectMapper objectMapper,
                                  UserDetailsService userDetailsService,
                                  AccountRepository accountRepository) {
        this.tenantService = tenantService;
        this.accountLoginRepository = accountLoginRepository;
        this.lobService = lobService;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Application initialization started ######### 123 ###############");

        try {
            // Add your initialization logic here
            // Examples:
            // - Validate configuration
            // - Initialize default data
            // - Warm up caches
            // - Check database connectivity
            // - Verify required entities exist
            
            //performInitialization();
            
            logger.info("Application initialization completed successfully");
        } catch (Exception e) {
            logger.error("Application initialization failed", e);
            // Decide whether to throw exception (fail fast) or continue
            // For critical initialization, you might want to throw:
            // throw new RuntimeException("Application initialization failed", e);
        } finally {
            // Clear security context after initialization
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * Performs the actual initialization tasks.
     * Override or extend this method to add specific initialization logic.
     */
    @Transactional
    private void performInitialization() {
        logger.debug("Performing application initialization tasks");
        
        AccountEntity account = accountRepository.findAdminAccount("sys", "sys", AccountNodeType.SYS_ADMIN).orElse(null);
        if (account != null) {
            setSecurityContext(account.getId());
            initDemoTenant();
        } else {
            logger.warn("System admin account not found. Skipping tenant initialization.");
        }

        AccountEntity account2 = accountRepository.findAdminAccount("demo", "demo", AccountNodeType.TNT_ADMIN).orElse(null);
        if (account2 != null) {
            setSecurityContext(account2.getId());
            // Additional initialization for demo tenant can be added here
            addLob("ACCIDENT", "Lob_Accident.json");
        }

    }

    /**
     * Loads LOB from JSON file and creates it if it doesn't exist.
     *
     * @param code the LOB code
     * @param fileName the JSON file name (without path, will be loaded from initdata/)
     */
    private void addLob(String code, String fileName) {
        if (lobService.getByCode(code) != null) {
            logger.debug("LOB '{}' already exists, skipping", code);
            return;
        }

        try {
            // Load JSON file from classpath:initdata/fileName
            ClassPathResource resource = new ClassPathResource("initdata/" + fileName);
            
            if (!resource.exists()) {
                logger.warn("LOB file not found: initdata/{}", fileName);
                return;
            }

            // Read and parse JSON file
            LobModel lobModel;
            try (InputStream inputStream = resource.getInputStream()) {
                lobModel = objectMapper.readValue(inputStream, LobModel.class);
            }

            // Ensure the code matches (in case file has different code)
            if (lobModel.getMpCode() == null || !lobModel.getMpCode().equals(code)) {
                logger.warn("LOB code mismatch in file '{}': expected '{}', found '{}'. Using expected code.", 
                        fileName, code, lobModel.getMpCode());
                lobModel.setMpCode(code);
            }

            // Create LOB using service
            lobService.create(lobModel);
            logger.info("Created LOB '{}' from file '{}'", code, fileName);
        } catch (Exception e) {
            logger.error("Failed to load and create LOB '{}' from file '{}': {}", 
                    code, fileName, e.getMessage(), e);
        }
    }


    /**
     * Finds an admin account login (SYS_ADMIN or TNT_ADMIN) for the given tenant.
     * Fetches all required relationships eagerly to avoid LazyInitializationException.
     *
     * @param tenantCode the tenant code
     * @return AccountLoginEntity if found, null otherwise
     */
    @Transactional
    private AccountLoginEntity findAdminAccountLogin(String tenantCode) {
        // Try to find SYS_ADMIN first
        AccountNodeType sysAdminNodeType = AccountNodeType.fromString(UserRole.SYS_ADMIN.getValue());
        if (sysAdminNodeType != null) {
            List<AccountLoginEntity> sysAdmins = accountLoginRepository.findByTenantAndUserRole(
                    tenantCode, sysAdminNodeType);
            
            if (sysAdmins != null && !sysAdmins.isEmpty()) {
                return sysAdmins.get(0);
            }
        }
        
        // If no SYS_ADMIN, try TNT_ADMIN
        AccountNodeType tntAdminNodeType = AccountNodeType.fromString(UserRole.TNT_ADMIN.getValue());
        if (tntAdminNodeType != null) {
            List<AccountLoginEntity> tntAdmins = accountLoginRepository.findByTenantAndUserRole(
                    tenantCode, tntAdminNodeType);
            
            if (tntAdmins != null && !tntAdmins.isEmpty()) {
                return tntAdmins.get(0);
            }
        }
        
        return null;
    }

    /**
     * Sets the security context with the given account login.
     *
     * @param accountLogin the account login entity
     */
    private void setSecurityContext(Long accountId) {
        // Build UserDetailsImpl from AccountLoginEntity
        logger.debug("IdentityResolutionFilter: Loading user details for accountId={}", accountId);
        UserDetails userDetails = userDetailsService.loadUserByUsername(Long.toString(accountId));
        /*
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //username это acc_account.id 
        // id анонимной учетки с правами

        Long accountId = Long.parseLong(username);
        String tenantCode = requestContext.getTenant();
        // tenant если есть то он уже проверен.
        if (tenantCode == null || tenantCode.isEmpty()) {
            throw new IllegalStateException("TenantContext not set");
        }

        String authClientId = requestContext.getClient();
        if (authClientId == null || authClientId.isEmpty()) {
            throw new IllegalStateException("ClientContext not set");
        }
        // Client не проверен.
        String login = requestContext.getLogin();

        AccountLoginEntity accountLoginEntity = accountLoginRepository.findByAll4Fields(tenantCode, authClientId, login, accountId).
            orElseThrow(() -> new UsernameNotFoundException("AccountLogin not found with id: " + accountId));
        //requestContext.setAccount(defaultAccountLogin.getAccount().getId().toString());
        requestContext.setAccount(username);
        // Инициализируем lazy-loaded поля внутри транзакции
        //initializeLazyFields(defaultAccountLogin, loginEntity);

        // Получаем роли продуктов для аккаунта
        Set<String> productRoles = getProductRoles(accountId);

        // Создаем UserDetails без пароля (JWT авторизация)
        UserDetails userDetails = UserDetailsImpl.build(accountLoginEntity, productRoles);
        return userDetails;
    }

        */

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("IdentityResolutionFilter: Successfully authenticated user: {}", userDetails.getUsername());
        
    }

    /**
     * Creates the "demo" tenant if it doesn't exist.
     */
    @Transactional
    private void initDemoTenant() {
        String demoTenantCode = "demo";
        String demoTenantName = "Demo Tenant";
        
        // Check if demo tenant already exists
        Optional<TenantEntity> existingTenant = tenantService.findByCode(demoTenantCode);
        if (existingTenant.isPresent()) {
            logger.debug("Demo tenant '{}' already exists", demoTenantCode);
            return;
        }
        
        // Create Tenant DTO using record constructor
        Tenant tenantDto = new Tenant(
                null, // id - will be generated
                demoTenantName,
                false, // isDeleted
                "JWT", // authType
                demoTenantCode,
                null, // createdAt - will be set by entity
                null  // updatedAt - will be set by entity
        );
        
        try {
            tenantService.createTenant(tenantDto);
            logger.info("Demo tenant '{}' created successfully with default client and accounts", demoTenantCode);
        } catch (Exception e) {
            logger.error("Failed to create demo tenant: {}", e.getMessage(), e);
        }
    }   
}

package ru.pt.zero_script;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.auth.service.TenantService;
import lombok.AllArgsConstructor;
import ru.pt.api.dto.auth.Tenant;
import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.refs.RecordStatus;
import ru.pt.api.dto.refs.TenantAuthType;
import ru.pt.api.service.auth.TenantConfig;
import ru.pt.auth.security.UserDetailsImpl;
import ru.pt.api.service.product.LobService;
/**
 * Runs {@link TestScenario1} once at startup when enabled (dev/test only).
 */
@Component
@Order(200)
//@ConditionalOnProperty(name = "app.test-scenario1.enabled", havingValue = "true")
@AllArgsConstructor
public class TestScenario1ApplicationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestScenario1ApplicationRunner.class);

    private final TenantService tenantService;
    private final Environment environment;
    private final UserDetailsService userDetailsService;
    private final LobService lobService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(ApplicationArguments args) {
        String tenantCode = environment.getProperty("app.test-scenario1.sys-tenant-code", "autotest_tnt");
        String authClientId = environment.getProperty("app.test-scenario1.sys-auth-client-id", "sys");


        log.info("########################  TestScenario1: starting (tenantCode={}, authClientId={})", tenantCode, authClientId);

        String accountId = "366";
        UserDetails userDetails = new UserDetailsImpl(
            0L,             // Long id
            "366",          // String username
            tenantCode,         // String tenantCode
            0L,             // Long tenantId
            0L,             // Long accountId
            "366",           // String accountName    
            1L,          // Long clientId
            "sys",        // String clientName
            "SYS_ADMIN",  // String userRole
            new HashSet<String>(), // Set<String> productRoles
            true, // boolean isDefault
            366L, // Long actingAccountId
            "366"); // String accountPath
/*

UserDetailsImpl(Long id, String username, String tenantCode, Long tenantId,
                          Long accountId, String accountName, Long clientId, String clientName,
                          String userRole, Set<String> productRoles, boolean isDefault, Long actingAccountId,
                            String accountPath) 
*/

        Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        long t0 = System.currentTimeMillis();
        try {
            Tenant testTenant = new Tenant(
                null, 
                "Test Tenant", 
                RecordStatus.ACTIVE, 
                TenantAuthType.LOCAL_JWT, 
                FileStorageType.DB, 
                "test_tnt", 
                LocalDateTime.now(), 
                LocalDateTime.now(), 
                new HashMap<>(), 
                new HashMap<>()
            );

            // take all *.json from ./lobs, parse LobModel, create (tenant id from security context — not testTenant.id())
            File directory = new File("lobs");
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    Long tenantId = ((UserDetailsImpl) userDetails).getTenantId();
                    for (File file : files) {
                        if (file.isFile() && file.getName().toLowerCase().endsWith(".json")) {
                            LobModel lobModel = objectMapper.readValue(file, LobModel.class);
                            lobService.create(tenantId, lobModel);
                        }
                    }
                }
            }
            //testTenant = tenantService.createTenant(testTenant);
            
            //tenantService.deleteTenant(testTenant);
            log.info("TestScenario1: finished successfully in {} ms", System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.error("TestScenario1: failed after {} ms", System.currentTimeMillis() - t0, e);
        }
    }

    private static void login2tenant(String tenantCode, String accountId) {
        /* 
        UserDetails userDetails = new UserDetailsImpl(
            0L,             // Long id
            accountId,          // String username
            tenantCode,         // String tenantCode
            0L,             // Long tenantId
            0L,             // Long accountId
            "366",           // String accountName    
            "1",          // Long clientId
            "sys",        // String clientName
            "SYS_ADMIN",  // String userRole
            new HashSet<String>(), // Set<String> productRoles
            true, // boolean isDefault
            Long.parseLong(accountId), // Long actingAccountId
            accountId); // String accountPath
        Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        */
    }
}

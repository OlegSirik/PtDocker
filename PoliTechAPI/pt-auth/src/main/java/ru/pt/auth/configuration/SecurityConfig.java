package ru.pt.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.pt.auth.security.filter.TenantResolutionFilter;
import ru.pt.auth.security.filter.IdentityResolutionFilter;
import ru.pt.auth.security.filter.AccountResolutionFilter;
import ru.pt.auth.security.filter.ContextCleanupFilter;
/**
 * Конфигурация Spring Security для JWT Authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    //private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityConfigurationProperties securityConfigurationProperties;
    private final TenantResolutionFilter tenantResolutionFilter;
    private final IdentityResolutionFilter identityResolutionFilter;
    private final AccountResolutionFilter accountResolutionFilter;
    private final ContextCleanupFilter contextCleanupFilter;

    public SecurityConfig(
                         SecurityConfigurationProperties securityConfigurationProperties,
                         TenantResolutionFilter tenantResolutionFilter,
                         IdentityResolutionFilter identityResolutionFilter,
                         AccountResolutionFilter accountResolutionFilter,
                         ContextCleanupFilter contextCleanupFilter) {
        //this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityConfigurationProperties = securityConfigurationProperties;
        this.tenantResolutionFilter = tenantResolutionFilter;
        this.identityResolutionFilter = identityResolutionFilter;
        this.accountResolutionFilter = accountResolutionFilter;
        this.contextCleanupFilter = contextCleanupFilter;
    }

   //  TODO: добавить авторизацию через Partner headers (проверить, что все работает)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var auth = http
            .csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authz -> {
                // Добавляем все публичные URL-ы из конфигурации
                if (!securityConfigurationProperties.getPublicUrls().isEmpty()) {
                    authz.requestMatchers(
                        securityConfigurationProperties.getPublicUrls().toArray(new String[0])
                    ).permitAll();
                }
                authz.anyRequest().authenticated();
            })
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(tenantResolutionFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(accountResolutionFilter, TenantResolutionFilter.class)
            .addFilterAfter(identityResolutionFilter, AccountResolutionFilter.class)
            //.addFilterAfter(jwtAuthenticationFilter, AccountResolutionFilter.class)
            .addFilterAfter(contextCleanupFilter, IdentityResolutionFilter.class);

        return auth.build();
    }
    
    
}


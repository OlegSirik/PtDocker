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
import ru.pt.auth.security.JwtAuthenticationFilter;

/**
 * Конфигурация Spring Security для JWT Authentication.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SecurityConfigurationProperties securityConfigurationProperties;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         SecurityConfigurationProperties securityConfigurationProperties) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityConfigurationProperties = securityConfigurationProperties;
    }

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
            .addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return auth.build();
    }
}


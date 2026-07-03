package ru.pt.auth.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.pt.auth.crypto.AppSecretsProperties;

@Configuration
@ComponentScan({"ru.pt.auth", "ru.pt.api"})
@EnableConfigurationProperties(AppSecretsProperties.class)
public class AuthModuleConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

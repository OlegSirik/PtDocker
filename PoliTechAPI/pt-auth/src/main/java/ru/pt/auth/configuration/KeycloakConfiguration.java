package ru.pt.auth.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class KeycloakConfiguration {
    
    /**
     * RestTemplate bean for Keycloak API calls
     * Separate bean to allow custom timeouts and error handling
     */
    @Bean(name = "keycloakRestTemplate")
    public RestTemplate keycloakRestTemplate(KeycloakProperties properties, RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(properties.getConnectionTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getReadTimeout()))
                .build();
    }
}

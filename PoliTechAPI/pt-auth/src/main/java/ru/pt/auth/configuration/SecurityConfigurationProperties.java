package ru.pt.auth.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Конфигурация для публичных URL-ов, доступных без аутентификации.
 */
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityConfigurationProperties {

    private List<String> publicUrls = new ArrayList<>();

    public List<String> getPublicUrls() {
        return publicUrls;
    }

    public void setPublicUrls(List<String> publicUrls) {
        this.publicUrls = publicUrls;
    }
}


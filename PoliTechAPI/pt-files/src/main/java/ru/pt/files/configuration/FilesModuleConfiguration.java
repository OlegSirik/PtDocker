package ru.pt.files.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import ru.pt.files.service.email.EmailClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@ComponentScan("ru.pt.files")
public class FilesModuleConfiguration {

    @Bean
    public Map<String, EmailClient> emailClientMap(List<EmailClient> emailClients) {
        return emailClients.stream()
                .collect(Collectors.toMap(
                        c -> c.getEmailGate().toLowerCase(Locale.ROOT),
                        Function.identity()));
    }
}

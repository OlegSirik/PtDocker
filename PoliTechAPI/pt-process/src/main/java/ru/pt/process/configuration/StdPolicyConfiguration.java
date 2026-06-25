package ru.pt.process.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.pt.api.dto.policy.InsuranceContractStdPolicyMapper;
import ru.pt.api.service.policy.StdPolicyMapper;

@Configuration
public class StdPolicyConfiguration {

    @Bean
    StdPolicyMapper insuranceContractStdPolicyMapper() {
        return new InsuranceContractStdPolicyMapper();
    }
}

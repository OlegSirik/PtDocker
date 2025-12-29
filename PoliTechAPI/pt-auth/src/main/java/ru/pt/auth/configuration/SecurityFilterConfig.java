package ru.pt.auth.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import ru.pt.auth.security.filter.TenantResolutionFilter;
import ru.pt.auth.security.context.RequestContext;
import ru.pt.auth.security.filter.IdentityResolutionFilter;
import ru.pt.auth.security.filter.AccountResolutionFilter;
import ru.pt.auth.security.filter.ContextCleanupFilter;
import ru.pt.auth.security.strategy.IdentitySourceStrategy;
import ru.pt.auth.service.AccountResolverService;
import ru.pt.auth.service.TenantSecurityConfigService;

@Configuration
public class SecurityFilterConfig {

    @Bean
    public TenantResolutionFilter tenantResolutionFilter(
            SecurityConfigurationProperties securityConfigurationProperties,
            RequestContext requestContext
    ) {
        return new TenantResolutionFilter(securityConfigurationProperties, requestContext);
    }

    @Bean
    public IdentityResolutionFilter identityResolutionFilter(
            SecurityConfigurationProperties securityConfigurationProperties,
            UserDetailsService userDetailsService,
            RequestContext requestContext
    ) {
        return new IdentityResolutionFilter(securityConfigurationProperties, userDetailsService, requestContext);
    }

    @Bean
    public AccountResolutionFilter accountResolutionFilter(
            SecurityConfigurationProperties securityConfigurationProperties,
            TenantSecurityConfigService tenantSecurityConfigService,
            List<IdentitySourceStrategy> identitySourceStrategies,
            AccountResolverService accountResolverService,
            RequestContext requestContext
    ) {
        return new AccountResolutionFilter(
            securityConfigurationProperties,
            tenantSecurityConfigService,
            identitySourceStrategies,
            accountResolverService,
            requestContext
        );
    }

    @Bean
    public ContextCleanupFilter contextCleanupFilter(
            RequestContext requestContext
    ) {
        return new ContextCleanupFilter(requestContext);
    }
}

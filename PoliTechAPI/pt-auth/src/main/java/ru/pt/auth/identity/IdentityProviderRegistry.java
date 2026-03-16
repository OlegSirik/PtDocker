package ru.pt.auth.identity;

import org.springframework.stereotype.Component;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.model.AuthType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр IdP-провайдеров по AuthType.
 * Если для конкретного типа провайдера нет — шаг синхронизации с IdP пропускается.
 */
@Component
public class IdentityProviderRegistry {

    private final Map<AuthType, IdentityProvider> providers = new EnumMap<>(AuthType.class);

    public IdentityProviderRegistry(List<IdentityProvider> providerList) {
        for (IdentityProvider provider : providerList) {
            providers.put(provider.supportedAuthType(), provider);
        }
    }

    /**
     * Вернуть провайдера для данного тенанта или null, если нет подходящего.
     */
    public IdentityProvider forTenant(TenantEntity tenant) {
        if (tenant == null || tenant.getAuthType() == null) {
            return null;
        }
        AuthType type;
        try {
            type = AuthType.valueOf(tenant.getAuthType());
        } catch (IllegalArgumentException ex) {
            return null;
        }
        return providers.get(type);
    }
}


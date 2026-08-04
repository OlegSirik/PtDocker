package ru.pt.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.ForbiddenException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.api.dto.exception.LlmUnavailableException;
import ru.pt.api.dto.exception.NotFoundException;
import ru.pt.api.dto.llm.TenantLlmApiKeyUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfig;
import ru.pt.api.dto.llm.TenantLlmConfigUpdateRequest;
import ru.pt.api.dto.llm.TenantLlmConfigView;
import ru.pt.api.dto.llm.TenantLlmProviderConfig;
import ru.pt.api.dto.llm.TenantLlmProviderConfigUpdate;
import ru.pt.api.dto.llm.TenantLlmProviderConfigView;
import ru.pt.api.dto.llm.TenantLlmRuntimeConfig;
import ru.pt.api.security.AuthenticatedUser;
import ru.pt.api.service.auth.AuthZ;
import ru.pt.api.service.auth.AuthorizationService;
import ru.pt.api.service.llm.TenantLlmConfigService;
import ru.pt.auth.crypto.SecretEncryptionService;
import ru.pt.auth.entity.AccountEntity;
import ru.pt.auth.entity.TenantEntity;
import ru.pt.auth.entity.UserRole;
import ru.pt.auth.llm.LlmApiKeyMask;
import ru.pt.auth.repository.AccountRepository;
import ru.pt.auth.repository.TenantRepository;
import ru.pt.auth.security.SecurityContextHelper;

import java.util.HashMap;
import java.util.Map;

@Service
public class TenantLlmConfigServiceImpl implements TenantLlmConfigService {

    private final TenantRepository tenantRepository;
    private final AccountRepository accountRepository;
    private final SecretEncryptionService encryptionService;
    private final ObjectMapper objectMapper;
    private final SecurityContextHelper securityContextHelper;
    private final AuthorizationService authorizationService;

    public TenantLlmConfigServiceImpl(
            TenantRepository tenantRepository,
            AccountRepository accountRepository,
            SecretEncryptionService encryptionService,
            ObjectMapper objectMapper,
            SecurityContextHelper securityContextHelper,
            AuthorizationService authorizationService) {
        this.tenantRepository = tenantRepository;
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
        this.objectMapper = objectMapper;
        this.securityContextHelper = securityContextHelper;
        this.authorizationService = authorizationService;
    }

    @Override
    @Transactional(readOnly = true)
    public TenantLlmRuntimeConfig resolve(Long tenantId) {
        TenantEntity tenant = loadTenant(tenantId);
        checkTenantAccess(tenant, AuthZ.Action.VIEW);
        return buildRuntimeConfig(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantLlmRuntimeConfig resolveForTest(String tenantCode) {
        TenantEntity tenant = requireTenantByCode(tenantCode, AuthZ.Action.MANAGE);
        return buildRuntimeConfig(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public TenantLlmConfigView getConfigView(String tenantCode) {
        TenantEntity tenant = requireTenantByCode(tenantCode, AuthZ.Action.VIEW);
        TenantLlmConfig config = loadDecryptedConfig(tenant);
        if (config == null) {
            TenantLlmConfigView empty = new TenantLlmConfigView();
            empty.setConfigured(false);
            empty.setEnabled(false);
            empty.setTimeoutMs(TenantLlmConfig.DEFAULT_TIMEOUT_MS);
            empty.setDefaultProvider(TenantLlmConfig.DEFAULT_PROVIDER);
            empty.setDefaultModel(TenantLlmConfig.DEFAULT_MODEL);
            return empty;
        }
        return toView(config);
    }

    @Override
    @Transactional
    public TenantLlmConfigView saveConfig(String tenantCode, TenantLlmConfigUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        TenantEntity tenant = requireTenantByCode(tenantCode, AuthZ.Action.MANAGE);
        TenantLlmConfig merged = mergeConfig(loadDecryptedConfig(tenant), request);
        persistConfig(tenant, merged);
        return toView(merged);
    }

    @Override
    @Transactional
    public TenantLlmConfigView updateApiKey(String tenantCode, TenantLlmApiKeyUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("Request body is required");
        }
        if (request.getProviderCode() == null || request.getProviderCode().isBlank()) {
            throw new BadRequestException("providerCode is required");
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new BadRequestException("apiKey is required");
        }
        TenantEntity tenant = requireTenantByCode(tenantCode, AuthZ.Action.MANAGE);
        TenantLlmConfig config = loadDecryptedConfig(tenant);
        if (config == null) {
            config = new TenantLlmConfig();
        }
        String providerCode = request.getProviderCode().trim();
        TenantLlmProviderConfig provider = config.getProviders().computeIfAbsent(
                providerCode, ignored -> new TenantLlmProviderConfig());
        provider.setApiKey(request.getApiKey().trim());
        persistConfig(tenant, config);
        return toView(config);
    }

    private TenantEntity requireTenantByCode(String tenantCode, AuthZ.Action action) {
        TenantEntity tenant = tenantRepository.findByCode(tenantCode.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantCode));
        checkTenantAccess(tenant, action);
        return tenant;
    }

    private void checkTenantAccess(TenantEntity tenant, AuthZ.Action action) {
        AuthenticatedUser user = getCurrentUser();
        if (!UserRole.SYS_ADMIN.getValue().equals(user.getUserRole())
                && !tenant.getId().equals(user.getTenantId())) {
            throw new ForbiddenException("Access denied to tenant");
        }
        AccountEntity tenantAccount = accountRepository.findByTenantId(tenant.getId())
                .orElseThrow(() -> new NotFoundException("Tenant account not found for tenant: " + tenant.getCode()));
        authorizationService.check(
                user,
                AuthZ.ResourceType.TENANT,
                String.valueOf(tenant.getId()),
                tenantAccount.getId(),
                action);
    }

    private AuthenticatedUser getCurrentUser() {
        return securityContextHelper.getAuthenticatedUser()
                .orElseThrow(() -> new ForbiddenException("Not authenticated"));
    }

    private TenantEntity loadTenant(Long tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found: " + tenantId));
    }

    private TenantLlmRuntimeConfig buildRuntimeConfig(TenantEntity tenant) {
        TenantLlmConfig config = loadDecryptedConfig(tenant);
        if (config == null) {
            throw new LlmUnavailableException();
        }
        if (!config.isEnabled()) {
            throw new LlmUnavailableException("DISABLED");
        }
        String providerCode = nonBlank(config.getDefaultProvider(), TenantLlmConfig.DEFAULT_PROVIDER);
        TenantLlmProviderConfig providerConfig = config.getProviders().get(providerCode);
        if (providerConfig == null
                || providerConfig.getApiKey() == null
                || providerConfig.getApiKey().isBlank()) {
            throw new LlmUnavailableException("NO_API_KEY");
        }

        TenantLlmRuntimeConfig runtime = new TenantLlmRuntimeConfig();
        runtime.setDefaultProvider(providerCode);
        runtime.setDefaultModel(nonBlank(config.getDefaultModel(), TenantLlmConfig.DEFAULT_MODEL));
        runtime.setTimeoutMs(config.getTimeoutMs() > 0 ? config.getTimeoutMs() : TenantLlmConfig.DEFAULT_TIMEOUT_MS);
        runtime.setProviders(config.getProviders());
        return runtime;
    }

    private TenantLlmConfig loadDecryptedConfig(TenantEntity tenant) {
        String encrypted = tenant.getLlmConfigEnc();
        if (encrypted == null || encrypted.isBlank()) {
            return null;
        }
        try {
            String json = encryptionService.decrypt(encrypted);
            return objectMapper.readValue(json, TenantLlmConfig.class);
        } catch (JsonProcessingException ex) {
            throw new InternalServerErrorException("Failed to parse tenant LLM config", ex);
        }
    }

    private void persistConfig(TenantEntity tenant, TenantLlmConfig config) {
        try {
            String json = objectMapper.writeValueAsString(config);
            tenant.setLlmConfigEnc(encryptionService.encrypt(json));
            tenantRepository.save(tenant);
        } catch (JsonProcessingException ex) {
            throw new InternalServerErrorException("Failed to serialize tenant LLM config", ex);
        }
    }

    private TenantLlmConfig mergeConfig(TenantLlmConfig existing, TenantLlmConfigUpdateRequest request) {
        TenantLlmConfig result = existing != null ? copyConfig(existing) : new TenantLlmConfig();

        if (request.getEnabled() != null) {
            result.setEnabled(request.getEnabled());
        }
        if (request.getDefaultProvider() != null && !request.getDefaultProvider().isBlank()) {
            result.setDefaultProvider(request.getDefaultProvider().trim());
        }
        if (request.getDefaultModel() != null && !request.getDefaultModel().isBlank()) {
            result.setDefaultModel(request.getDefaultModel().trim());
        }
        if (request.getTimeoutMs() != null && request.getTimeoutMs() > 0) {
            result.setTimeoutMs(request.getTimeoutMs());
        }

        if (request.getProviders() != null) {
            for (Map.Entry<String, TenantLlmProviderConfigUpdate> entry : request.getProviders().entrySet()) {
                String code = entry.getKey();
                TenantLlmProviderConfigUpdate update = entry.getValue();
                if (update == null) {
                    continue;
                }
                TenantLlmProviderConfig provider = result.getProviders().computeIfAbsent(
                        code, ignored -> new TenantLlmProviderConfig());
                if (update.getBaseUrl() != null) {
                    provider.setBaseUrl(update.getBaseUrl().isBlank() ? null : update.getBaseUrl().trim());
                }
                if (update.getDefaultModel() != null) {
                    provider.setDefaultModel(
                            update.getDefaultModel().isBlank() ? null : update.getDefaultModel().trim());
                }
                if (!LlmApiKeyMask.isOmittedOrMasked(update.getApiKey())) {
                    provider.setApiKey(update.getApiKey().trim());
                }
            }
        }
        return result;
    }

    private TenantLlmConfig copyConfig(TenantLlmConfig source) {
        TenantLlmConfig copy = new TenantLlmConfig();
        copy.setEnabled(source.isEnabled());
        copy.setDefaultProvider(source.getDefaultProvider());
        copy.setDefaultModel(source.getDefaultModel());
        copy.setTimeoutMs(source.getTimeoutMs());
        Map<String, TenantLlmProviderConfig> providers = new HashMap<>();
        for (Map.Entry<String, TenantLlmProviderConfig> entry : source.getProviders().entrySet()) {
            TenantLlmProviderConfig src = entry.getValue();
            if (src == null) {
                continue;
            }
            TenantLlmProviderConfig dst = new TenantLlmProviderConfig();
            dst.setBaseUrl(src.getBaseUrl());
            dst.setApiKey(src.getApiKey());
            dst.setDefaultModel(src.getDefaultModel());
            providers.put(entry.getKey(), dst);
        }
        copy.setProviders(providers);
        return copy;
    }

    private TenantLlmConfigView toView(TenantLlmConfig config) {
        TenantLlmConfigView view = new TenantLlmConfigView();
        view.setConfigured(true);
        view.setEnabled(config.isEnabled());
        view.setDefaultProvider(config.getDefaultProvider());
        view.setDefaultModel(config.getDefaultModel());
        view.setTimeoutMs(config.getTimeoutMs());

        Map<String, TenantLlmProviderConfigView> providers = new HashMap<>();
        for (Map.Entry<String, TenantLlmProviderConfig> entry : config.getProviders().entrySet()) {
            TenantLlmProviderConfig src = entry.getValue();
            if (src == null) {
                continue;
            }
            TenantLlmProviderConfigView dst = new TenantLlmProviderConfigView();
            dst.setBaseUrl(src.getBaseUrl());
            dst.setDefaultModel(src.getDefaultModel());
            boolean keyConfigured = src.getApiKey() != null && !src.getApiKey().isBlank();
            dst.setApiKeyConfigured(keyConfigured);
            dst.setApiKeyMasked(keyConfigured ? LlmApiKeyMask.mask(src.getApiKey()) : null);
            providers.put(entry.getKey(), dst);
        }
        view.setProviders(providers);
        return view;
    }

    private static String nonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}

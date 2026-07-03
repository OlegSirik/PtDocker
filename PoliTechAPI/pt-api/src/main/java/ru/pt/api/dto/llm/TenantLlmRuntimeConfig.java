package ru.pt.api.dto.llm;

import java.util.HashMap;
import java.util.Map;

/**
 * Эффективная runtime-конфигурация LLM для вызова провайдера.
 */
public class TenantLlmRuntimeConfig {

    private String defaultProvider;
    private String defaultModel;
    private int timeoutMs;
    private Map<String, TenantLlmProviderConfig> providers = new HashMap<>();

    public String getDefaultProvider() {
        return defaultProvider;
    }

    public void setDefaultProvider(String defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Map<String, TenantLlmProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, TenantLlmProviderConfig> providers) {
        this.providers = providers != null ? providers : new HashMap<>();
    }

    public TenantLlmProviderConfig providerConfig(String providerCode) {
        return providers.get(providerCode);
    }
}

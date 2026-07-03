package ru.pt.api.dto.llm;

import java.util.HashMap;
import java.util.Map;

public class TenantLlmConfigUpdateRequest {

    private Boolean enabled;
    private String defaultProvider;
    private String defaultModel;
    private Integer timeoutMs;
    private Map<String, TenantLlmProviderConfigUpdate> providers = new HashMap<>();

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

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

    public Integer getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Map<String, TenantLlmProviderConfigUpdate> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, TenantLlmProviderConfigUpdate> providers) {
        this.providers = providers != null ? providers : new HashMap<>();
    }
}

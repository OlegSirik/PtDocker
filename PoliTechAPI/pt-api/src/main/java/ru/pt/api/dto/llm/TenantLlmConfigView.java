package ru.pt.api.dto.llm;

import java.util.HashMap;
import java.util.Map;

public class TenantLlmConfigView {

    private boolean enabled;
    private String defaultProvider;
    private String defaultModel;
    private int timeoutMs;
    private boolean configured;
    private Map<String, TenantLlmProviderConfigView> providers = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
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

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public Map<String, TenantLlmProviderConfigView> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, TenantLlmProviderConfigView> providers) {
        this.providers = providers != null ? providers : new HashMap<>();
    }
}

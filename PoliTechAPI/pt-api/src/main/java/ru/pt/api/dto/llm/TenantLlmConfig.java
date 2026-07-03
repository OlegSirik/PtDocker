package ru.pt.api.dto.llm;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory модель конфигурации LLM tenant после расшифровки.
 */
public class TenantLlmConfig {

    public static final int DEFAULT_TIMEOUT_MS = 120_000;
    public static final String DEFAULT_PROVIDER = "routerai";
    public static final String DEFAULT_MODEL = "deepseek/deepseek-v4-flash";

    private boolean enabled;
    private String defaultProvider = DEFAULT_PROVIDER;
    private String defaultModel = DEFAULT_MODEL;
    private int timeoutMs = DEFAULT_TIMEOUT_MS;
    private Map<String, TenantLlmProviderConfig> providers = new HashMap<>();

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

    public Map<String, TenantLlmProviderConfig> getProviders() {
        return providers;
    }

    public void setProviders(Map<String, TenantLlmProviderConfig> providers) {
        this.providers = providers != null ? providers : new HashMap<>();
    }
}

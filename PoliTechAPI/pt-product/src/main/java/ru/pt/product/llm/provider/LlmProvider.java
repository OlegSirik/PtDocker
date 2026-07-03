package ru.pt.product.llm.provider;

import ru.pt.api.dto.llm.TenantLlmProviderConfig;

public interface LlmProvider {

    String getCode();

    LlmCompletionResult complete(
            LlmCompletionRequest request,
            TenantLlmProviderConfig providerConfig,
            int timeoutMs);
}

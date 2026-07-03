package ru.pt.product.llm.provider;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.llm.TenantLlmProviderConfig;
import ru.pt.api.dto.llm.TenantLlmRuntimeConfig;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmGateway {

    private final Map<String, LlmProvider> providersByCode;

    public LlmGateway(List<LlmProvider> providers) {
        this.providersByCode = providers.stream()
                .collect(Collectors.toMap(LlmProvider::getCode, Function.identity()));
    }

    public LlmCompletionResult complete(
            LlmCompletionRequest request,
            TenantLlmRuntimeConfig tenantConfig,
            String providerCodeOverride,
            String modelOverride) {
        String code = providerCodeOverride != null && !providerCodeOverride.isBlank()
                ? providerCodeOverride
                : tenantConfig.getDefaultProvider();
        LlmProvider provider = providersByCode.get(code);
        if (provider == null) {
            throw new BadRequestException("Unknown LLM provider: " + code);
        }
        TenantLlmProviderConfig providerConfig = tenantConfig.providerConfig(code);
        if (providerConfig == null) {
            throw new BadRequestException("Provider is not configured: " + code);
        }
        String resolvedModel = modelOverride != null && !modelOverride.isBlank()
                ? modelOverride
                : resolveDefaultModel(tenantConfig, code, providerConfig);
        return provider.complete(
                new LlmCompletionRequest(
                        request.messages(),
                        resolvedModel,
                        request.temperature(),
                        request.maxTokens(),
                        request.jsonMode()),
                providerConfig,
                tenantConfig.getTimeoutMs());
    }

    private String resolveDefaultModel(
            TenantLlmRuntimeConfig tenantConfig,
            String providerCode,
            TenantLlmProviderConfig providerConfig) {
        if (providerConfig.getDefaultModel() != null && !providerConfig.getDefaultModel().isBlank()) {
            return providerConfig.getDefaultModel();
        }
        return tenantConfig.getDefaultModel();
    }
}

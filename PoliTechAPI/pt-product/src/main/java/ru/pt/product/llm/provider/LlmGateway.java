package ru.pt.product.llm.provider;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.product.llm.configuration.LlmProperties;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmGateway {

    private final LlmProperties properties;
    private final Map<String, LlmProvider> providersByCode;

    public LlmGateway(LlmProperties properties, List<LlmProvider> providers) {
        this.properties = properties;
        this.providersByCode = providers.stream()
                .collect(Collectors.toMap(LlmProvider::getCode, Function.identity()));
    }

    public LlmCompletionResult complete(LlmCompletionRequest request, String providerCode, String model) {
        if (!properties.isEnabled()) {
            throw new BadRequestException("LLM integration is disabled (app.llm.enabled=false)");
        }
        String code = providerCode != null && !providerCode.isBlank()
                ? providerCode
                : properties.getDefaultProvider();
        LlmProvider provider = providersByCode.get(code);
        if (provider == null) {
            throw new BadRequestException("Unknown LLM provider: " + code);
        }
        String resolvedModel = model != null && !model.isBlank() ? model : resolveDefaultModel(code);
        return provider.complete(new LlmCompletionRequest(
                request.messages(),
                resolvedModel,
                request.temperature(),
                request.maxTokens(),
                request.jsonMode()));
    }

    private String resolveDefaultModel(String providerCode) {
        LlmProperties.ProviderConfig cfg = properties.getProviders().get(providerCode);
        if (cfg != null && cfg.getDefaultModel() != null && !cfg.getDefaultModel().isBlank()) {
            return cfg.getDefaultModel();
        }
        return properties.getDefaultModel();
    }
}

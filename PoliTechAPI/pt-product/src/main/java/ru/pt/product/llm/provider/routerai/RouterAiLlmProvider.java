package ru.pt.product.llm.provider.routerai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import ru.pt.product.llm.configuration.LlmProperties;
import ru.pt.product.llm.provider.OpenAiCompatibleLlmProvider;

/**
 * RouterAI — OpenAI-compatible API.
 * POST https://routerai.ru/api/v1/chat/completions
 * model: deepseek/deepseek-v4-flash
 */
@Component
public class RouterAiLlmProvider extends OpenAiCompatibleLlmProvider {

    private static final String CODE = "routerai";

    public RouterAiLlmProvider(
            LlmProperties properties,
            ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder) {
        super(properties, objectMapper, restTemplateBuilder);
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://routerai.ru/api/v1";
    }

    @Override
    protected String apiKeyConfigHint() {
        return "app.llm.providers.routerai.api-key";
    }
}

package ru.pt.product.llm.provider.routerai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import ru.pt.product.llm.provider.OpenAiCompatibleLlmProvider;

/**
 * RouterAI — OpenAI-compatible API.
 */
@Component
public class RouterAiLlmProvider extends OpenAiCompatibleLlmProvider {

    private static final String CODE = "routerai";

    public RouterAiLlmProvider(
            ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder) {
        super(objectMapper, restTemplateBuilder);
    }

    @Override
    public String getCode() {
        return CODE;
    }

    @Override
    protected String defaultBaseUrl() {
        return "https://routerai.ru/api/v1";
    }
}

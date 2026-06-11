package ru.pt.product.llm.provider.deepseek;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import ru.pt.product.llm.configuration.LlmProperties;
import ru.pt.product.llm.provider.OpenAiCompatibleLlmProvider;

@Component
public class DeepSeekLlmProvider extends OpenAiCompatibleLlmProvider {

    private static final String CODE = "deepseek";

    public DeepSeekLlmProvider(
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
        return "https://api.deepseek.com";
    }

    @Override
    protected String apiKeyConfigHint() {
        return "app.llm.providers.deepseek.api-key";
    }
}

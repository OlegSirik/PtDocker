package ru.pt.product.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.exception.InternalServerErrorException;
import ru.pt.product.llm.configuration.LlmProperties;

import java.time.Duration;

public abstract class OpenAiCompatibleLlmProvider implements LlmProvider {

    private final LlmProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    protected OpenAiCompatibleLlmProvider(
            LlmProperties properties,
            ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .build();
    }

    protected abstract String defaultBaseUrl();

    protected abstract String apiKeyConfigHint();

    @Override
    public LlmCompletionResult complete(LlmCompletionRequest request) {
        String code = getCode();
        LlmProperties.ProviderConfig cfg = properties.getProviders().get(code);
        if (cfg == null || cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            throw new BadRequestException("API key is not configured (" + apiKeyConfigHint() + ")");
        }
        String baseUrl = cfg.getBaseUrl() != null && !cfg.getBaseUrl().isBlank()
                ? cfg.getBaseUrl()
                : defaultBaseUrl();
        String url = baseUrl.replaceAll("/$", "") + "/chat/completions";

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", request.model());
        body.put("temperature", request.temperature() != null ? request.temperature() : 0.1);
        if (request.maxTokens() != null) {
            body.put("max_tokens", request.maxTokens());
        }
        ArrayNode messages = body.putArray("messages");
        for (LlmMessage message : request.messages()) {
            ObjectNode msg = messages.addObject();
            msg.put("role", message.role());
            msg.put("content", message.content());
        }
        if (request.jsonMode()) {
            ObjectNode responseFormat = body.putObject("response_format");
            responseFormat.put("type", "json_object");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cfg.getApiKey());

        long started = System.currentTimeMillis();
        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    JsonNode.class);
            JsonNode root = response.getBody();
            if (root == null) {
                throw new InternalServerErrorException("Empty response from " + code);
            }
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            JsonNode usage = root.path("usage");
            int promptTokens = usage.path("prompt_tokens").asInt(0);
            int completionTokens = usage.path("completion_tokens").asInt(0);
            return new LlmCompletionResult(
                    content,
                    request.model(),
                    promptTokens,
                    completionTokens,
                    System.currentTimeMillis() - started);
        } catch (RestClientException ex) {
            throw new InternalServerErrorException(code + " API error: " + ex.getMessage());
        }
    }
}

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
import ru.pt.api.dto.llm.TenantLlmProviderConfig;

import java.time.Duration;

public abstract class OpenAiCompatibleLlmProvider implements LlmProvider {

    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    protected OpenAiCompatibleLlmProvider(
            ObjectMapper objectMapper,
            RestTemplateBuilder restTemplateBuilder) {
        this.objectMapper = objectMapper;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    protected abstract String defaultBaseUrl();

    @Override
    public LlmCompletionResult complete(
            LlmCompletionRequest request,
            TenantLlmProviderConfig providerConfig,
            int timeoutMs) {
        String code = getCode();
        if (providerConfig == null
                || providerConfig.getApiKey() == null
                || providerConfig.getApiKey().isBlank()) {
            throw new BadRequestException("API key is not configured for provider: " + code);
        }
        String baseUrl = providerConfig.getBaseUrl() != null && !providerConfig.getBaseUrl().isBlank()
                ? providerConfig.getBaseUrl()
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
        headers.setBearerAuth(providerConfig.getApiKey());

        RestTemplate restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(timeoutMs))
                .setReadTimeout(Duration.ofMillis(timeoutMs))
                .build();

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

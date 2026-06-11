package ru.pt.product.llm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonExtractor {

    private static final Pattern FENCED_JSON = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    private JsonExtractor() {
    }

    public static JsonNode parseObject(String raw, ObjectMapper objectMapper) throws Exception {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Empty LLM response");
        }
        String trimmed = raw.trim();
        Matcher matcher = FENCED_JSON.matcher(trimmed);
        if (matcher.find()) {
            trimmed = matcher.group(1).trim();
        }
        JsonNode node = objectMapper.readTree(trimmed);
        if (!node.isObject()) {
            throw new IllegalArgumentException("Expected JSON object");
        }
        return node;
    }
}

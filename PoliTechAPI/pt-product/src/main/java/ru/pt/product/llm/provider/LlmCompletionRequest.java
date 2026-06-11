package ru.pt.product.llm.provider;

import java.util.List;

public record LlmCompletionRequest(
        List<LlmMessage> messages,
        String model,
        Double temperature,
        Integer maxTokens,
        boolean jsonMode
) {
}

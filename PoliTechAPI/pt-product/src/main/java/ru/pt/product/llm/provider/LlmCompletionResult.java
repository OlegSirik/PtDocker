package ru.pt.product.llm.provider;

public record LlmCompletionResult(
        String content,
        String model,
        int promptTokens,
        int completionTokens,
        long latencyMs
) {
}

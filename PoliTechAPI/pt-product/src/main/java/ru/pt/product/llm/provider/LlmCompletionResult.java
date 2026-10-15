package ru.pt.product.llm.provider;

public record LlmCompletionResult(
        String content,
        String model,
        int promptTokens,
        int completionTokens,
        long latencyMs,
        String finishReason
) {
    public LlmCompletionResult(
            String content,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs) {
        this(content, model, promptTokens, completionTokens, latencyMs, null);
    }

    public boolean truncated() {
        return finishReason != null && "length".equalsIgnoreCase(finishReason.trim());
    }
}

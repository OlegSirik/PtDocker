package ru.pt.product.llm.provider;

public interface LlmProvider {

    String getCode();

    LlmCompletionResult complete(LlmCompletionRequest request);
}

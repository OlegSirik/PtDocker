package ru.pt.product.llm.processor;

import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;

import java.util.List;
import java.util.Set;

public interface LlmResponseProcessor {

    LlmTaskType getTaskType();

    LlmProcessedResult process(String rawContent, Set<String> knownVarCodes, List<PvVar> productVars);
}

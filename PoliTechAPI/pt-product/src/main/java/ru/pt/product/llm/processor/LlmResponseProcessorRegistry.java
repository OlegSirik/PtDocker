package ru.pt.product.llm.processor;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.exception.BadRequestException;
import ru.pt.api.dto.llm.LlmTaskType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LlmResponseProcessorRegistry {

    private final Map<LlmTaskType, LlmResponseProcessor> processors;

    public LlmResponseProcessorRegistry(List<LlmResponseProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(LlmResponseProcessor::getTaskType, Function.identity()));
    }

    public LlmResponseProcessor get(LlmTaskType taskType) {
        LlmResponseProcessor processor = processors.get(taskType);
        if (processor == null) {
            throw new BadRequestException("Unsupported LLM task type: " + taskType);
        }
        return processor;
    }
}

package ru.pt.product.llm.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.product.llm.provider.LlmMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptAssembler {

    private static final Map<LlmTaskType, String> PROMPT_CACHE = new ConcurrentHashMap<>();

    private final VarsContextFormatter varsContextFormatter;

    public PromptAssembler(VarsContextFormatter varsContextFormatter) {
        this.varsContextFormatter = varsContextFormatter;
    }

    public List<LlmMessage> assemble(
            LlmTaskType taskType,
            String userMessage,
            ProductVersionModel product) {
        String system = loadPrompt(taskType);
        String varsText = varsContextFormatter.format(product.getVars());
        String user = """
                Запрос пользователя:
                %s

                Продукт: %s (%s), версия %s

                Словарь переменных передаётся в формате:
                varCode: Описание переменной

                Доступные переменные (product.vars):
                %s
                """.formatted(
                userMessage,
                nullToEmpty(product.getCode()),
                nullToEmpty(product.getLob()),
                product.getVersionNo() != null ? product.getVersionNo() : "",
                varsText);
        return List.of(
                new LlmMessage("system", system),
                new LlmMessage("user", user));
    }

    private String loadPrompt(LlmTaskType taskType) {
        return PROMPT_CACHE.computeIfAbsent(taskType, type -> {
            String fileName = "llm/prompts/" + type.name().toLowerCase() + ".txt";
            try {
                ClassPathResource resource = new ClassPathResource(fileName);
                return resource.getContentAsString(StandardCharsets.UTF_8);
            } catch (IOException ex) {
                throw new IllegalStateException("Cannot load prompt: " + fileName, ex);
            }
        });
    }

    private static String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}

package ru.pt.product.llm.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.LobCover;
import ru.pt.api.dto.product.LobModel;
import ru.pt.api.dto.product.LobVar;
import ru.pt.api.dto.product.ProductVersionModel;
import ru.pt.api.dto.product.PvCover;
import ru.pt.api.dto.product.PvPackage;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.provider.LlmMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PromptAssembler {

    private static final Map<LlmTaskType, String> PROMPT_CACHE = new ConcurrentHashMap<>();

    private final VarsContextFormatter varsContextFormatter;
    private final ObjectMapper objectMapper;

    public PromptAssembler(VarsContextFormatter varsContextFormatter, ObjectMapper objectMapper) {
        this.varsContextFormatter = varsContextFormatter;
        this.objectMapper = objectMapper;
    }

    public List<LlmMessage> assemble(
            LlmTaskType taskType,
            String userMessage,
            ProductVersionModel product,
            Long tenantId) {
        String system = loadPrompt(taskType);
        String varsText = taskType == LlmTaskType.RULE
                ? varsContextFormatter.formatForRules(product.getVars(), tenantId)
                : varsContextFormatter.format(product.getVars(), tenantId);
        String celRulesHint = taskType == LlmTaskType.RULE
                ? """

                Правила CEL для condition:
                - проверка null: только голый varCode (ph_doc_typeCode != null), НЕ str("varCode") != null
                - NUMBER для сравнений: num("varCode"); перед > < >= <= сначала varCode != null
                - STRING для in [...] и сравнения строк: str("varCode")
                - если у переменной указаны «допустимые значения», используй только коды из списка;
                  при отсутствии подходящего кода верни {"error": true, "message": "..."}, не придумывай новые коды
                """
                : "";
        String user = """
                Запрос пользователя:
                %s

                Продукт: %s (%s), версия %s
                %s
                Словарь переменных (varCode: описание [тип, в CEL: ...]):
                %s
                """.formatted(
                userMessage,
                nullToEmpty(product.getCode()),
                nullToEmpty(product.getLob()),
                product.getVersionNo() != null ? product.getVersionNo() : "",
                celRulesHint,
                varsText);
        return List.of(
                new LlmMessage("system", system),
                new LlmMessage("user", user));
    }

    public List<LlmMessage> assembleForLob(
            LlmTaskType taskType,
            String userMessage,
            String lobCode,
            String lobName,
            List<PvVar> vars,
            Long tenantId) {
        String system = loadPrompt(taskType);
        String varsText = taskType == LlmTaskType.RULE
                ? varsContextFormatter.formatForRules(vars, tenantId)
                : varsContextFormatter.format(vars, tenantId);
        String celRulesHint = taskType == LlmTaskType.RULE
                ? """

                Правила CEL для condition:
                - проверка null: только голый varCode (ph_doc_typeCode != null), НЕ str("varCode") != null
                - NUMBER для сравнений: num("varCode"); перед > < >= <= сначала varCode != null
                - STRING для in [...] и сравнения строк: str("varCode")
                - если у переменной указаны «допустимые значения», используй только коды из списка;
                  при отсутствии подходящего кода верни {"error": true, "message": "..."}, не придумывай новые коды
                """
                : "";
        String user = """
                Запрос пользователя:
                %s

                LOB: %s (%s)
                %s
                Словарь переменных (varCode: описание [тип, в CEL: ...]):
                %s
                """.formatted(
                userMessage,
                nullToEmpty(lobCode),
                nullToEmpty(lobName),
                celRulesHint,
                varsText);
        return List.of(
                new LlmMessage("system", system),
                new LlmMessage("user", user));
    }

    public List<LlmMessage> assembleForSchema(
            String userMessage,
            String contractCode,
            List<LobVar> schemaVars,
            String parentVarCodeHint) {
        String system = loadPrompt(LlmTaskType.SCHEMA);
        String schemaText = varsContextFormatter.formatSchema(schemaVars);
        String parentHint = (parentVarCodeHint != null && !parentVarCodeHint.isBlank())
                ? "\nПредпочтительный родитель (parentVarCode): " + parentVarCodeHint.trim() + "\n"
                : "";
        String user = """
                Запрос пользователя:
                %s

                Модель договора (contractCode): %s
                %s
                Текущая схема (varCode | varName | varPath | varType | id | parent_id):
                %s
                """.formatted(
                userMessage,
                nullToEmpty(contractCode),
                parentHint,
                schemaText.isBlank() ? "(пусто)" : schemaText);
        return List.of(
                new LlmMessage("system", system),
                new LlmMessage("user", user));
    }

    public List<LlmMessage> assembleCalculator(String userMessage, CalculatorModel currentCalculator) {
        String system = loadPrompt(LlmTaskType.CALCULATOR);
        String calculatorJson = serializeCalculatorContext(currentCalculator);
        String user = """
                Запрос пользователя:
                %s

                currentCalculator:
                %s
                """.formatted(userMessage, calculatorJson);
        return List.of(
                new LlmMessage("system", system),
                new LlmMessage("user", user));
    }

    public List<LlmMessage> assembleForPackages(
            String userMessage,
            ProductVersionModel product,
            LobModel lob) {
        String system = loadPrompt(LlmTaskType.PACKAGE);
        String existingPackages = formatExistingPackages(product != null ? product.getPackages() : null);
        String lobCovers = formatLobCovers(lob != null ? lob.getMpCovers() : null);
        String user = """
                Запрос пользователя:
                %s

                Продукт: %s (%s), версия %s

                Существующие пакеты:
                %s

                Каталог покрытий LOB (используй только эти code для covers):
                %s
                """.formatted(
                userMessage,
                nullToEmpty(product != null ? product.getCode() : null),
                nullToEmpty(product != null ? product.getLob() : null),
                product != null && product.getVersionNo() != null ? product.getVersionNo() : "",
                existingPackages,
                lobCovers);
        return List.of(
                new LlmMessage("system", system),
                new LlmMessage("user", user));
    }

    private String formatExistingPackages(List<PvPackage> packages) {
        if (packages == null || packages.isEmpty()) {
            return "(пусто)";
        }
        StringBuilder sb = new StringBuilder();
        for (PvPackage pkg : packages) {
            if (pkg == null) {
                continue;
            }
            sb.append("- ").append(nullToEmpty(pkg.getCode()))
                    .append(" | ").append(nullToEmpty(pkg.getName()))
                    .append(" | covers: ");
            List<PvCover> covers = pkg.getCovers();
            if (covers == null || covers.isEmpty()) {
                sb.append("(нет)");
            } else {
                List<String> codes = new ArrayList<>();
                for (PvCover cover : covers) {
                    if (cover != null && cover.getCode() != null && !cover.getCode().isBlank()) {
                        codes.add(cover.getCode());
                    }
                }
                sb.append(codes.isEmpty() ? "(нет)" : String.join(", ", codes));
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    private String formatLobCovers(List<LobCover> covers) {
        if (covers == null || covers.isEmpty()) {
            return "(пусто — можно предложить code, но лучше уточнить у пользователя)";
        }
        StringBuilder sb = new StringBuilder();
        for (LobCover cover : covers) {
            if (cover == null || cover.getCoverCode() == null || cover.getCoverCode().isBlank()) {
                continue;
            }
            sb.append("- ").append(cover.getCoverCode().trim())
                    .append(" | ").append(nullToEmpty(cover.getCoverName()))
                    .append('\n');
        }
        String text = sb.toString().trim();
        return text.isBlank() ? "(пусто)" : text;
    }

    private String serializeCalculatorContext(CalculatorModel calculator) {
        if (calculator == null) {
            return "{}";
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", calculator.getId());
            payload.put("productId", calculator.getProductId());
            payload.put("productCode", calculator.getProductCode());
            payload.put("versionNo", calculator.getVersionNo());
            payload.put("packageNo", calculator.getPackageNo());
            payload.put("llmText", calculator.getLlmText());
            payload.put("vars", slimVars(calculator.getVars()));
            payload.put("formulas", calculator.getFormulas() != null ? calculator.getFormulas() : List.of());
            payload.put("coefficients", calculator.getCoefficients() != null ? calculator.getCoefficients() : List.of());
            // Compact JSON: pretty-print inflates prompt tokens and slows the model.
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Cannot serialize currentCalculator for LLM", ex);
        }
    }

    private List<Map<String, String>> slimVars(List<PvVar> vars) {
        if (vars == null || vars.isEmpty()) {
            return List.of();
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (PvVar var : vars) {
            if (var == null || var.getVarCode() == null || var.getVarCode().isBlank()) {
                continue;
            }
            Map<String, String> item = new LinkedHashMap<>();
            item.put("varCode", var.getVarCode());
            item.put("varName", firstNonBlank(var.getVarName(), var.getName()));
            result.add(item);
        }
        return result;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
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

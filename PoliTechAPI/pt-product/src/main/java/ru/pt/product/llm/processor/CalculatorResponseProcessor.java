package ru.pt.product.llm.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.llm.LlmCalculatorDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CalculatorResponseProcessor implements LlmResponseProcessor {

    private static final Pattern IDENTIFIER = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

    private final ObjectMapper objectMapper;

    public CalculatorResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmTaskType getTaskType() {
        return LlmTaskType.CALCULATOR;
    }

    @Override
    public LlmProcessedResult process(String rawContent, Set<String> knownVarCodes, List<PvVar> productVars) {
        try {
            JsonNode root = JsonExtractor.parseObject(rawContent, objectMapper);
            JsonNode calculatorNode = root.path("calculator");
            if (calculatorNode.isMissingNode()) {
                return LlmProcessedResult.fail(List.of("Missing 'calculator' object in response"));
            }
            List<String> warnings = unknownVarWarnings(calculatorNode.toString(), knownVarCodes);
            LlmCalculatorDraft draft = new LlmCalculatorDraft();
            draft.setCalculator(calculatorNode);
            return LlmProcessedResult.ok(draft, warnings);
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private List<String> unknownVarWarnings(String text, Set<String> knownVarCodes) {
        List<String> warnings = new ArrayList<>();
        Matcher matcher = IDENTIFIER.matcher(text);
        while (matcher.find()) {
            String id = matcher.group(1);
            if (id.length() < 3 || Character.isUpperCase(id.charAt(0))) {
                continue;
            }
            if (!knownVarCodes.contains(id)) {
                warnings.add("Variable not in product.vars: " + id);
            }
        }
        return warnings.stream().distinct().toList();
    }
}

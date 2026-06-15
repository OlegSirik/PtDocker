package ru.pt.product.llm.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CoefficientColumn;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.FormulaDef;
import ru.pt.api.dto.calculator.FormulaLine;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CalculatorResponseProcessor implements LlmResponseProcessor {

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
            if (root.path("error").asBoolean(false)) {
                String message = root.path("message").asText("Unknown variable in calculator response");
                return LlmProcessedResult.fail(List.of(message));
            }

            CalculatorModel calculator = parseCalculator(root);
            if (calculator == null) {
                return LlmProcessedResult.fail(List.of("В ответе нет объекта calculator"));
            }

            Set<String> responseVarCodes = mergeVarCodes(knownVarCodes, calculator.getVars());
            List<String> unknownVars = findUnknownVars(
                    calculator.getFormulas(),
                    calculator.getCoefficients(),
                    responseVarCodes);
            if (!unknownVars.isEmpty()) {
                String code = unknownVars.getFirst();
                return LlmProcessedResult.fail(List.of(
                        "Уточните условие, условие '" + code + "' не найдено в контексте"));
            }

            if (isEmptyCalculator(calculator)) {
                return LlmProcessedResult.fail(List.of("В ответе пустой калькулятор"));
            }

            return LlmProcessedResult.ok(calculator, List.of());
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private CalculatorModel parseCalculator(JsonNode root) throws com.fasterxml.jackson.core.JsonProcessingException {
        if (root.has("calculator") && root.get("calculator").isObject()) {
            return objectMapper.treeToValue(root.get("calculator"), CalculatorModel.class);
        }
        if (root.has("formulas") || root.has("coefficients") || root.has("vars")) {
            return objectMapper.treeToValue(root, CalculatorModel.class);
        }
        return null;
    }

    private static boolean isEmptyCalculator(CalculatorModel calculator) {
        boolean hasFormulas = calculator.getFormulas() != null && !calculator.getFormulas().isEmpty();
        boolean hasCoefficients = calculator.getCoefficients() != null && !calculator.getCoefficients().isEmpty();
        return !hasFormulas && !hasCoefficients;
    }

    private static Set<String> mergeVarCodes(Set<String> knownVarCodes, List<PvVar> responseVars) {
        Set<String> merged = new LinkedHashSet<>();
        if (knownVarCodes != null) {
            merged.addAll(knownVarCodes);
        }
        if (responseVars != null) {
            merged.addAll(responseVars.stream()
                    .filter(v -> v != null && v.getVarCode() != null && !v.getVarCode().isBlank())
                    .map(PvVar::getVarCode)
                    .collect(Collectors.toSet()));
        }
        return merged;
    }

    private List<String> findUnknownVars(
            List<FormulaDef> formulas,
            List<CoefficientDef> coefficients,
            Set<String> knownVarCodes) {
        Set<String> unknown = new LinkedHashSet<>();
        if (formulas != null) {
            for (FormulaDef formula : formulas) {
                if (formula.getLines() == null) {
                    continue;
                }
                for (FormulaLine line : formula.getLines()) {
                    checkVarCode(line.getExpressionLeft(), knownVarCodes, unknown);
                    checkVarCode(line.getExpressionRight(), knownVarCodes, unknown);
                    checkVarCode(line.getExpressionResult(), knownVarCodes, unknown);
                }
            }
        }
        if (coefficients != null) {
            for (CoefficientDef coefficient : coefficients) {
                if (coefficient.getColumns() == null) {
                    continue;
                }
                for (CoefficientColumn column : coefficient.getColumns()) {
                    checkVarCode(column.getVarCode(), knownVarCodes, unknown);
                }
            }
        }
        return new ArrayList<>(unknown);
    }

    private void checkVarCode(String value, Set<String> knownVarCodes, Set<String> unknown) {
        if (value == null || value.isBlank() || isNumericLiteral(value)) {
            return;
        }
        if (!knownVarCodes.contains(value)) {
            unknown.add(value);
        }
    }

    private static boolean isNumericLiteral(String value) {
        try {
            new BigDecimal(value.trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}

package ru.pt.product.llm.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.calculator.CoefficientColumn;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.FormulaDef;
import ru.pt.api.dto.calculator.FormulaLine;
import ru.pt.api.dto.llm.LlmCalculatorDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

            List<FormulaDef> formulas = parseFormulas(root.path("formulas"));
            List<CoefficientDef> coefficients = parseCoefficients(root.path("coefficients"));

            List<String> unknownVars = findUnknownVars(formulas, coefficients, knownVarCodes);
            if (!unknownVars.isEmpty()) {
                String code = unknownVars.getFirst();
                return LlmProcessedResult.fail(List.of(
                        "Уточните условие, условие '" + code + "' не найдено в контексте"));
            }

            LlmCalculatorDraft draft = new LlmCalculatorDraft();
            draft.setFormulas(formulas);
            draft.setCoefficients(coefficients);
            return LlmProcessedResult.ok(draft, List.of());
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private List<FormulaDef> parseFormulas(JsonNode formulasNode) {
        List<FormulaDef> formulas = new ArrayList<>();
        if (!formulasNode.isArray()) {
            return formulas;
        }
        for (JsonNode formulaNode : formulasNode) {
            FormulaDef formula = new FormulaDef();
            formula.setVarCode(textOrNull(formulaNode, "varCode"));
            formula.setVarName(textOrNull(formulaNode, "varName"));
            formula.setLines(parseFormulaLines(formulaNode.path("lines")));
            formulas.add(formula);
        }
        return formulas;
    }

    private List<FormulaLine> parseFormulaLines(JsonNode linesNode) {
        List<FormulaLine> lines = new ArrayList<>();
        if (!linesNode.isArray()) {
            return lines;
        }
        for (JsonNode lineNode : linesNode) {
            FormulaLine line = new FormulaLine();
            if (lineNode.hasNonNull("nr")) {
                line.setNr(lineNode.get("nr").asLong());
            }
            line.setConditionLeft(textOrNull(lineNode, "conditionLeft"));
            line.setConditionOperator(textOrNull(lineNode, "conditionOperator"));
            line.setConditionRight(textOrNull(lineNode, "conditionRight"));
            line.setExpressionResult(textOrNull(lineNode, "expressionResult"));
            line.setExpressionLeft(textOrNull(lineNode, "expressionLeft"));
            line.setExpressionOperator(textOrNull(lineNode, "expressionOperator"));
            line.setExpressionRight(textOrNull(lineNode, "expressionRight"));
            line.setPostProcessor(textOrNull(lineNode, "postProcessor"));
            lines.add(line);
        }
        return lines;
    }

    private List<CoefficientDef> parseCoefficients(JsonNode coefficientsNode) {
        List<CoefficientDef> coefficients = new ArrayList<>();
        if (!coefficientsNode.isArray()) {
            return coefficients;
        }
        for (JsonNode coefficientNode : coefficientsNode) {
            CoefficientDef coefficient = new CoefficientDef();
            coefficient.setVarCode(textOrNull(coefficientNode, "varCode"));
            coefficient.setVarName(textOrNull(coefficientNode, "varName"));
            coefficient.setAltVarCode(textOrNull(coefficientNode, "altVarCode"));
            if (coefficientNode.has("altVarValue") && !coefficientNode.get("altVarValue").isNull()) {
                coefficient.setAltVarValue(coefficientNode.get("altVarValue").decimalValue());
            }
            coefficient.setErrorTextIfNotFound(textOrNull(coefficientNode, "errorTextIfNotFound"));
            coefficient.setColumns(parseCoefficientColumns(coefficientNode.path("columns")));
            coefficients.add(coefficient);
        }
        return coefficients;
    }

    private List<CoefficientColumn> parseCoefficientColumns(JsonNode columnsNode) {
        List<CoefficientColumn> columns = new ArrayList<>();
        if (!columnsNode.isArray()) {
            return columns;
        }
        for (JsonNode columnNode : columnsNode) {
            CoefficientColumn column = new CoefficientColumn();
            column.setVarCode(textOrNull(columnNode, "varCode"));
            column.setVarDataType(textOrNull(columnNode, "varDataType"));
            if (columnNode.hasNonNull("nr")) {
                column.setNr(columnNode.get("nr").asInt());
            }
            column.setConditionOperator(textOrNull(columnNode, "conditionOperator"));
            column.setSortOrder(textOrNull(columnNode, "sortOrder"));
            columns.add(column);
        }
        return columns;
    }

    private List<String> findUnknownVars(
            List<FormulaDef> formulas,
            List<CoefficientDef> coefficients,
            Set<String> knownVarCodes) {
        Set<String> unknown = new LinkedHashSet<>();
        for (FormulaDef formula : formulas) {
            for (FormulaLine line : formula.getLines()) {
                checkVarCode(line.getExpressionLeft(), knownVarCodes, unknown);
                checkVarCode(line.getExpressionRight(), knownVarCodes, unknown);
                checkVarCode(line.getExpressionResult(), knownVarCodes, unknown);
            }
        }
        for (CoefficientDef coefficient : coefficients) {
            for (CoefficientColumn column : coefficient.getColumns()) {
                checkVarCode(column.getVarCode(), knownVarCodes, unknown);
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

    private static String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text.isBlank() ? null : text;
    }
}

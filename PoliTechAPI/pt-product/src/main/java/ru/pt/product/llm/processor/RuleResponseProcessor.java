package ru.pt.product.llm.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.llm.LlmRuleDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;
import ru.pt.rules.service.CelRuleEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleResponseProcessor implements LlmResponseProcessor {

    private static final Pattern IDENTIFIER = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
    private static final Set<String> CEL_KEYWORDS = Set.of(
            "true", "false", "null", "in", "has", "num", "str");

    private final ObjectMapper objectMapper;
    private final CelRuleEngine celRuleEngine;

    public RuleResponseProcessor(ObjectMapper objectMapper, CelRuleEngine celRuleEngine) {
        this.objectMapper = objectMapper;
        this.celRuleEngine = celRuleEngine;
    }

    @Override
    public LlmTaskType getTaskType() {
        return LlmTaskType.RULE;
    }

    @Override
    public LlmProcessedResult process(String rawContent, Set<String> knownVarCodes, List<PvVar> productVars) {
        try {
            JsonNode root = JsonExtractor.parseObject(rawContent, objectMapper);
            JsonNode ruleNode = root.path("rule");
            if (ruleNode.isMissingNode()) {
                return LlmProcessedResult.fail(List.of("Missing 'rule' object in response"));
            }
            String code = text(ruleNode, "code");
            String name = text(ruleNode, "name");
            String condition = text(ruleNode, "condition");
            String message = text(ruleNode, "message");
            if (code.isBlank() || name.isBlank() || condition.isBlank()) {
                return LlmProcessedResult.fail(List.of("rule.code, rule.name and rule.condition are required"));
            }
            String celError = celRuleEngine.validateExpressionOrError(condition);
            if (celError != null) {
                return LlmProcessedResult.fail(List.of("CEL compile: " + celError));
            }
            List<String> warnings = unknownVarWarnings(condition, knownVarCodes);
            LlmRuleDraft draft = new LlmRuleDraft();
            draft.setCode(code);
            draft.setName(name);
            draft.setCondition(condition);
            draft.setMessage(message);
            return LlmProcessedResult.ok(draft, warnings);
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private List<String> unknownVarWarnings(String condition, Set<String> knownVarCodes) {
        List<String> warnings = new ArrayList<>();
        Matcher matcher = IDENTIFIER.matcher(condition);
        while (matcher.find()) {
            String id = matcher.group(1);
            if (CEL_KEYWORDS.contains(id)) {
                continue;
            }
            if (!knownVarCodes.contains(id)) {
                warnings.add("Variable not in product.vars: " + id);
            }
        }
        return warnings;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }
}

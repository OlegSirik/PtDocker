package ru.pt.product.llm.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.llm.LlmCoverDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class CoverResponseProcessor implements LlmResponseProcessor {

    private final ObjectMapper objectMapper;

    public CoverResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmTaskType getTaskType() {
        return LlmTaskType.COVER;
    }

    @Override
    public LlmProcessedResult process(String rawContent, Set<String> knownVarCodes, List<PvVar> productVars) {
        try {
            JsonNode root = JsonExtractor.parseObject(rawContent, objectMapper);
            JsonNode coverNode = root.path("cover");
            if (coverNode.isMissingNode()) {
                return LlmProcessedResult.fail(List.of("Missing 'cover' object in response"));
            }
            String code = text(coverNode, "code");
            String name = text(coverNode, "name");
            if (code.isBlank() || name.isBlank()) {
                return LlmProcessedResult.fail(List.of("cover.code and cover.name are required"));
            }
            List<PvVar> vars = objectMapper.convertValue(
                    coverNode.path("vars"),
                    new TypeReference<List<PvVar>>() {});
            if (vars == null) {
                vars = List.of();
            }
            List<String> warnings = new ArrayList<>();
            for (PvVar var : vars) {
                if (var.getVarCode() != null && knownVarCodes.contains(var.getVarCode())) {
                    warnings.add("varCode already exists: " + var.getVarCode());
                }
            }
            LlmCoverDraft draft = new LlmCoverDraft();
            draft.setCode(code);
            draft.setName(name);
            if (coverNode.has("isMandatory") && !coverNode.path("isMandatory").isNull()) {
                draft.setIsMandatory(coverNode.path("isMandatory").asBoolean());
            }
            draft.setVars(vars);
            return LlmProcessedResult.ok(draft, warnings);
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }
}

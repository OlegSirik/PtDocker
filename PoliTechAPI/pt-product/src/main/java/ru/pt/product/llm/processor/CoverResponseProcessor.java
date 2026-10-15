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
            if (root.path("error").asBoolean(false)) {
                String message = text(root, "message");
                return LlmProcessedResult.fail(List.of(
                        message.isBlank() ? "LLM refused to generate covers" : message));
            }

            List<JsonNode> coverNodes = new ArrayList<>();
            JsonNode coversNode = root.path("covers");
            if (coversNode.isArray() && !coversNode.isEmpty()) {
                coversNode.forEach(coverNodes::add);
            } else if (root.has("cover") && root.get("cover").isObject()) {
                coverNodes.add(root.get("cover"));
            } else {
                return LlmProcessedResult.fail(List.of("Missing 'covers' array or 'cover' object in response"));
            }

            List<LlmCoverDraft> drafts = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            Set<String> draftCodes = new java.util.HashSet<>();

            for (int i = 0; i < coverNodes.size(); i++) {
                JsonNode coverNode = coverNodes.get(i);
                String prefix = "covers[" + i + "]";
                String code = text(coverNode, "code");
                String name = text(coverNode, "name");
                if (code.isBlank() || name.isBlank()) {
                    errors.add(prefix + ": code and name are required");
                    continue;
                }
                if (draftCodes.contains(code)) {
                    warnings.add("Duplicate cover code in draft: " + code);
                }
                draftCodes.add(code);

                List<PvVar> vars = objectMapper.convertValue(
                        coverNode.path("vars"),
                        new TypeReference<List<PvVar>>() {});
                if (vars == null) {
                    vars = List.of();
                }
                for (PvVar var : vars) {
                    if (var.getVarCode() != null && knownVarCodes.contains(var.getVarCode())) {
                        warnings.add("varCode already exists: " + var.getVarCode());
                    }
                }

                LlmCoverDraft draft = new LlmCoverDraft();
                draft.setCode(code);
                draft.setName(name);
                draft.setRisks(text(coverNode, "risks"));
                if (coverNode.has("isMandatory") && !coverNode.path("isMandatory").isNull()) {
                    draft.setIsMandatory(coverNode.path("isMandatory").asBoolean());
                }
                draft.setVars(vars);
                drafts.add(draft);
            }

            if (!errors.isEmpty()) {
                return LlmProcessedResult.fail(errors);
            }
            if (drafts.isEmpty()) {
                return LlmProcessedResult.fail(List.of("covers list is empty"));
            }
            return LlmProcessedResult.ok(drafts, warnings);
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }
}

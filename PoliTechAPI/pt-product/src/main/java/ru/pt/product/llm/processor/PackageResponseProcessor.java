package ru.pt.product.llm.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.llm.LlmPackageCoverDraft;
import ru.pt.api.dto.llm.LlmPackageDeductibleDraft;
import ru.pt.api.dto.llm.LlmPackageDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PackageResponseProcessor implements LlmResponseProcessor {

    private final ObjectMapper objectMapper;

    public PackageResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmTaskType getTaskType() {
        return LlmTaskType.PACKAGE;
    }

    @Override
    public LlmProcessedResult process(String rawContent, Set<String> knownVarCodes, List<PvVar> productVars) {
        try {
            JsonNode root = JsonExtractor.parseObject(rawContent, objectMapper);
            if (root.path("error").asBoolean(false)) {
                String message = text(root, "message");
                return LlmProcessedResult.fail(List.of(
                        message.isBlank() ? "LLM refused to generate packages" : message));
            }

            List<JsonNode> packageNodes = new ArrayList<>();
            JsonNode packagesNode = root.path("packages");
            if (packagesNode.isArray() && !packagesNode.isEmpty()) {
                packagesNode.forEach(packageNodes::add);
            } else if (root.has("package") && root.get("package").isObject()) {
                packageNodes.add(root.get("package"));
            } else {
                return LlmProcessedResult.fail(List.of("Missing 'packages' array or 'package' object in response"));
            }

            List<LlmPackageDraft> drafts = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            Set<String> draftCodes = new HashSet<>();

            for (int i = 0; i < packageNodes.size(); i++) {
                JsonNode pkgNode = packageNodes.get(i);
                String prefix = "packages[" + i + "]";
                String code = text(pkgNode, "code");
                String name = text(pkgNode, "name");
                if (code.isBlank() || name.isBlank()) {
                    errors.add(prefix + ": code and name are required");
                    continue;
                }
                if (draftCodes.contains(code)) {
                    warnings.add("Duplicate package code in draft: " + code);
                }
                draftCodes.add(code);

                LlmPackageDraft draft = new LlmPackageDraft();
                draft.setCode(code);
                draft.setName(name);
                draft.setCovers(parseCovers(pkgNode.path("covers"), prefix, errors, warnings));
                drafts.add(draft);
            }

            if (!errors.isEmpty()) {
                return LlmProcessedResult.fail(errors);
            }
            if (drafts.isEmpty()) {
                return LlmProcessedResult.fail(List.of("packages list is empty"));
            }
            return LlmProcessedResult.ok(drafts, warnings);
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private List<LlmPackageCoverDraft> parseCovers(
            JsonNode coversNode,
            String pkgPrefix,
            List<String> errors,
            List<String> warnings) {
        List<LlmPackageCoverDraft> covers = new ArrayList<>();
        if (coversNode == null || !coversNode.isArray()) {
            return covers;
        }
        Set<String> coverCodes = new HashSet<>();
        for (int i = 0; i < coversNode.size(); i++) {
            JsonNode coverNode = coversNode.get(i);
            String prefix = pkgPrefix + ".covers[" + i + "]";
            String code = text(coverNode, "code");
            if (code.isBlank()) {
                errors.add(prefix + ": code is required");
                continue;
            }
            if (coverCodes.contains(code)) {
                warnings.add(prefix + ": duplicate cover code " + code);
            }
            coverCodes.add(code);

            LlmPackageCoverDraft cover = new LlmPackageCoverDraft();
            cover.setCode(code);
            if (coverNode.has("isMandatory") && !coverNode.path("isMandatory").isNull()) {
                cover.setIsMandatory(coverNode.path("isMandatory").asBoolean());
            }
            cover.setWaitingPeriod(text(coverNode, "waitingPeriod"));
            cover.setCoverageTerm(text(coverNode, "coverageTerm"));
            if (coverNode.has("isDeductibleMandatory") && !coverNode.path("isDeductibleMandatory").isNull()) {
                cover.setIsDeductibleMandatory(coverNode.path("isDeductibleMandatory").asBoolean());
            }
            cover.setDeductibles(parseDeductibles(coverNode.path("deductibles"), prefix, errors, warnings));
            covers.add(cover);
        }
        return covers;
    }

    private List<LlmPackageDeductibleDraft> parseDeductibles(
            JsonNode deductiblesNode,
            String coverPrefix,
            List<String> errors,
            List<String> warnings) {
        List<LlmPackageDeductibleDraft> deductibles = new ArrayList<>();
        if (deductiblesNode == null || !deductiblesNode.isArray()) {
            return deductibles;
        }
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < deductiblesNode.size(); i++) {
            JsonNode dedNode = deductiblesNode.get(i);
            String prefix = coverPrefix + ".deductibles[" + i + "]";
            if (!dedNode.has("id") || dedNode.path("id").isNull()) {
                errors.add(prefix + ": id is required");
                continue;
            }
            long id = dedNode.path("id").asLong();
            String text = text(dedNode, "text");
            if (text.isBlank()) {
                errors.add(prefix + ": text is required");
                continue;
            }
            if (ids.contains(id)) {
                warnings.add(prefix + ": duplicate deductible id " + id);
            }
            ids.add(id);

            LlmPackageDeductibleDraft ded = new LlmPackageDeductibleDraft();
            ded.setId(id);
            ded.setText(text);
            deductibles.add(ded);
        }
        return deductibles;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }
}

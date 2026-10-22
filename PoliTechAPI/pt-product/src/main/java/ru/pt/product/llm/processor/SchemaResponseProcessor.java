package ru.pt.product.llm.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import ru.pt.api.dto.llm.LlmSchemaAttributeDraft;
import ru.pt.api.dto.llm.LlmSchemaDraft;
import ru.pt.api.dto.llm.LlmSchemaRefDictDraft;
import ru.pt.api.dto.llm.LlmSchemaRefDictItemDraft;
import ru.pt.api.dto.llm.LlmTaskType;
import ru.pt.api.dto.product.PvVar;
import ru.pt.product.llm.util.JsonExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SchemaResponseProcessor implements LlmResponseProcessor {

    private static final Set<String> ALLOWED_VAR_TYPES = Set.of("IN", "OBJECT", "MAGIC", "CONST", "VAR");
    private static final Set<String> ALLOWED_DATA_TYPES = Set.of("STRING", "NUMBER", "DATE", "TIME", "OBJECT");
    private static final Pattern REFDICT_CODE = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9_-]{0,49}$");

    private final ObjectMapper objectMapper;

    public SchemaResponseProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmTaskType getTaskType() {
        return LlmTaskType.SCHEMA;
    }

    @Override
    public LlmProcessedResult process(String rawContent, Set<String> knownVarCodes, List<PvVar> productVars) {
        try {
            JsonNode root = JsonExtractor.parseObject(rawContent, objectMapper);
            if (root.path("error").asBoolean(false)) {
                String message = text(root, "message");
                return LlmProcessedResult.fail(List.of(
                        message.isBlank() ? "LLM refused to generate attributes" : message));
            }
            JsonNode attrsNode = root.path("attributes");
            if (!attrsNode.isArray()) {
                return LlmProcessedResult.fail(List.of("Missing 'attributes' array in response"));
            }
            List<LlmSchemaAttributeDraft> attributes = objectMapper.convertValue(
                    attrsNode,
                    new TypeReference<List<LlmSchemaAttributeDraft>>() {});
            if (attributes == null || attributes.isEmpty()) {
                return LlmProcessedResult.fail(List.of("attributes array is empty"));
            }

            List<LlmSchemaRefDictDraft> refdicts = new ArrayList<>();
            JsonNode refdictsNode = root.path("refdicts");
            if (refdictsNode.isArray()) {
                refdicts = objectMapper.convertValue(
                        refdictsNode,
                        new TypeReference<List<LlmSchemaRefDictDraft>>() {});
                if (refdicts == null) {
                    refdicts = new ArrayList<>();
                }
            }

            Map<String, Long> existingIdsByCode = new HashMap<>();
            if (productVars != null) {
                for (PvVar var : productVars) {
                    if (var != null && var.getVarCode() != null && !var.getVarCode().isBlank()) {
                        existingIdsByCode.put(var.getVarCode(), var.getId());
                    }
                }
            }

            Set<String> known = knownVarCodes != null ? new HashSet<>(knownVarCodes) : new HashSet<>();
            Set<String> draftCodes = new HashSet<>();
            Set<String> draftRefdictCodes = new HashSet<>();
            List<String> warnings = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < refdicts.size(); i++) {
                LlmSchemaRefDictDraft dict = refdicts.get(i);
                String prefix = "refdicts[" + i + "]";
                if (dict == null) {
                    errors.add(prefix + " is null");
                    continue;
                }
                String code = trim(dict.getCode());
                String name = trim(dict.getName());
                if (code.isBlank()) {
                    errors.add(prefix + ".code is required");
                } else if (!REFDICT_CODE.matcher(code).matches()) {
                    errors.add(prefix + ".code invalid: " + code);
                } else if (!draftRefdictCodes.add(code)) {
                    errors.add(prefix + ".code duplicated in draft: " + code);
                }
                if (name.isBlank()) {
                    errors.add(prefix + ".name is required");
                }
                dict.setCode(code);
                dict.setName(name);

                List<LlmSchemaRefDictItemDraft> items = dict.getItems();
                if (items == null) {
                    items = new ArrayList<>();
                    dict.setItems(items);
                }
                if (items.isEmpty()) {
                    warnings.add(prefix + " has no items");
                }
                Set<String> itemCodes = new HashSet<>();
                for (int j = 0; j < items.size(); j++) {
                    LlmSchemaRefDictItemDraft item = items.get(j);
                    String ip = prefix + ".items[" + j + "]";
                    if (item == null) {
                        errors.add(ip + " is null");
                        continue;
                    }
                    String ic = trim(item.getCode());
                    String in = trim(item.getName());
                    if (ic.isBlank()) {
                        errors.add(ip + ".code is required");
                    } else if (!itemCodes.add(ic)) {
                        errors.add(ip + ".code duplicated: " + ic);
                    }
                    if (in.isBlank()) {
                        errors.add(ip + ".name is required");
                    }
                    item.setCode(ic);
                    item.setName(in);
                }
            }

            for (int i = 0; i < attributes.size(); i++) {
                LlmSchemaAttributeDraft attr = attributes.get(i);
                String prefix = "attributes[" + i + "]";
                if (attr == null) {
                    errors.add(prefix + " is null");
                    continue;
                }
                String varCode = trim(attr.getVarCode());
                String varName = trim(attr.getVarName());
                String varPath = trim(attr.getVarPath());
                String varType = trim(attr.getVarType()).toUpperCase(Locale.ROOT);
                String varDataType = trim(attr.getVarDataType()).toUpperCase(Locale.ROOT);

                if (varCode.isBlank()) {
                    errors.add(prefix + ".varCode is required");
                } else if (known.contains(varCode) || draftCodes.contains(varCode)) {
                    warnings.add("varCode already exists or duplicated in draft: " + varCode);
                }
                if (varName.isBlank()) {
                    errors.add(prefix + ".varName is required");
                }
                if (varPath.isBlank()) {
                    errors.add(prefix + ".varPath is required");
                }
                if (varType.isBlank()) {
                    errors.add(prefix + ".varType is required");
                } else if (!ALLOWED_VAR_TYPES.contains(varType)) {
                    errors.add(prefix + ".varType unsupported: " + varType);
                }
                if (varDataType.isBlank()) {
                    varDataType = "OBJECT".equals(varType) ? "OBJECT" : "STRING";
                    attr.setVarDataType(varDataType);
                } else if (!ALLOWED_DATA_TYPES.contains(varDataType)) {
                    errors.add(prefix + ".varDataType unsupported: " + varDataType);
                }

                attr.setVarCode(varCode);
                attr.setVarName(varName);
                attr.setVarPath(varPath);
                attr.setVarType(varType);
                attr.setVarDataType(varDataType);
                String varList = trim(attr.getVarList());
                attr.setVarList(varList.isBlank() ? null : varList);
                if (attr.getIsSystem() == null) {
                    attr.setIsSystem(false);
                }

                String parentVarCode = trim(attr.getParentVarCode());
                if (!parentVarCode.isBlank()) {
                    attr.setParentVarCode(parentVarCode);
                    if (attr.getParentId() == null && existingIdsByCode.containsKey(parentVarCode)) {
                        attr.setParentId(existingIdsByCode.get(parentVarCode));
                    } else if (attr.getParentId() == null && !draftCodes.contains(parentVarCode)
                            && !known.contains(parentVarCode)) {
                        // Parent may be an earlier OBJECT in the same draft
                        boolean earlierInDraft = attributes.subList(0, i).stream()
                                .anyMatch(a -> a != null && parentVarCode.equals(trim(a.getVarCode())));
                        if (!earlierInDraft) {
                            errors.add(prefix + ".parentVarCode not found: " + parentVarCode);
                        }
                    }
                } else if (attr.getParentId() != null) {
                    boolean parentKnown = productVars != null && productVars.stream()
                            .anyMatch(v -> v != null && attr.getParentId().equals(v.getId()));
                    if (!parentKnown) {
                        warnings.add(prefix + ".parent_id not found in current schema: " + attr.getParentId());
                    }
                }

                if (!varCode.isBlank()) {
                    draftCodes.add(varCode);
                }
            }

            if (!errors.isEmpty()) {
                return LlmProcessedResult.fail(errors);
            }

            LlmSchemaDraft draft = new LlmSchemaDraft();
            draft.setAttributes(attributes);
            draft.setRefdicts(refdicts);
            return LlmProcessedResult.ok(draft, warnings);
        } catch (Exception ex) {
            return LlmProcessedResult.fail(List.of("Parse error: " + ex.getMessage()));
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText("").trim();
    }

    private static String trim(String value) {
        return value != null ? value.trim() : "";
    }
}

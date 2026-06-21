package ru.pt.product.llm.prompt;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.product.PvVar;
import ru.pt.db.service.RefDataService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VarsContextFormatter {

    private static final Set<String> EXCLUDED_VAR_TYPES = Set.of("OBJECT", "TEXT");

    private final RefDataService refDataService;

    public VarsContextFormatter(RefDataService refDataService) {
        this.refDataService = refDataService;
    }

    public String formatForRules(List<PvVar> vars, Long tenantId) {
        if (vars == null || vars.isEmpty()) {
            return "";
        }
        return vars.stream()
                .filter(this::isIncluded)
                .map(var -> toRuleLine(var, tenantId))
                .collect(Collectors.joining("\n"));
    }

    public String format(List<PvVar> vars, Long tenantId) {
        if (vars == null || vars.isEmpty()) {
            return "";
        }
        return vars.stream()
                .filter(this::isIncluded)
                .map(var -> toLine(var, tenantId))
                .collect(Collectors.joining("\n"));
    }

    public Set<String> varCodes(List<PvVar> vars) {
        if (vars == null) {
            return Set.of();
        }
        return vars.stream()
                .filter(this::isIncluded)
                .map(PvVar::getVarCode)
                .collect(Collectors.toSet());
    }

    public String formatMerged(List<PvVar> productVars, List<PvVar> calculatorVars, Long tenantId) {
        return mergeVars(productVars, calculatorVars).values().stream()
                .map(var -> toLine(var, tenantId))
                .collect(Collectors.joining("\n"));
    }

    public Set<String> mergedVarCodes(List<PvVar> productVars, List<PvVar> calculatorVars) {
        return mergeVars(productVars, calculatorVars).keySet();
    }

    private Map<String, PvVar> mergeVars(List<PvVar> productVars, List<PvVar> calculatorVars) {
        Map<String, PvVar> merged = new LinkedHashMap<>();
        if (productVars != null) {
            for (PvVar var : productVars) {
                if (isIncluded(var)) {
                    merged.put(var.getVarCode(), var);
                }
            }
        }
        if (calculatorVars != null) {
            for (PvVar var : calculatorVars) {
                if (isIncluded(var)) {
                    merged.put(var.getVarCode(), var);
                }
            }
        }
        return merged;
    }

    private String toLine(PvVar var, Long tenantId) {
        String base = var.getVarCode() + ": " + firstNonBlank(var.getVarName(), var.getName());
        String allowed = formatAllowedValues(resolveRefValues(var, tenantId));
        return allowed.isEmpty() ? base : base + allowed;
    }

    private String toRuleLine(PvVar var, Long tenantId) {
        String code = var.getVarCode();
        String desc = firstNonBlank(var.getVarName(), var.getName());
        String dataType = var.getVarDataType() != null
                ? var.getVarDataType().name()
                : "STRING";
        String celAccessor = isNumericDataType(dataType)
                ? "num(\"" + code + "\")"
                : "str(\"" + code + "\")";
        String allowed = formatAllowedValues(resolveRefValues(var, tenantId));
        return code + ": " + desc + " [" + dataType + ", в CEL: " + celAccessor + allowed + "]";
    }

    /**
     * varList — код справочника; varValue — CSV допустимых кодов (тот же список, что IN_LIST).
     * Пустой varValue — весь справочник.
     */
    private Map<String, String> resolveRefValues(PvVar var, Long tenantId) {
        if (var == null || var.getVarList() == null || var.getVarList().isBlank()) {
            return Map.of();
        }
        if (tenantId == null) {
            return Map.of();
        }
        Map<String, String> refData = refDataService.getRefData(tenantId, var.getVarList().trim());
        if (refData.isEmpty()) {
            return Map.of();
        }
        String varValue = var.getVarValue();
        if (varValue != null && !varValue.isBlank()) {
            List<String> filter = Arrays.stream(varValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            if (!filter.isEmpty()) {
                refData.entrySet().removeIf(entry -> !filter.contains(entry.getKey()));
            }
        }
        return refData;
    }

    private String formatAllowedValues(Map<String, String> refValues) {
        if (refValues == null || refValues.isEmpty()) {
            return "";
        }
        String entries = refValues.entrySet().stream()
                .map(e -> e.getKey() + "=\"" + escapeQuotes(e.getValue()) + "\"")
                .collect(Collectors.joining(", "));
        String codes = refValues.keySet().stream()
                .map(code -> "\"" + escapeQuotes(code) + "\"")
                .collect(Collectors.joining(", "));
        return ", допустимые значения: " + entries + " (в CEL только коды: [" + codes + "])";
    }

    private static String escapeQuotes(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "'");
    }

    private static boolean isNumericDataType(String dataType) {
        return "NUMBER".equalsIgnoreCase(dataType);
    }

    private boolean isIncluded(PvVar var) {
        if (var == null || var.getIsDeleted()) {
            return false;
        }
        if (var.getVarCode() == null || var.getVarCode().isBlank()) {
            return false;
        }
        String varType = var.getVarType() != null ? var.getVarType().toUpperCase(Locale.ROOT) : "";
        return !EXCLUDED_VAR_TYPES.contains(varType);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}

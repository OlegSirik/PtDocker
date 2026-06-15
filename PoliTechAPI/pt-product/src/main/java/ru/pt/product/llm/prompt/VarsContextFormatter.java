package ru.pt.product.llm.prompt;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.product.PvVar;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VarsContextFormatter {

    private static final Set<String> EXCLUDED_VAR_TYPES = Set.of("OBJECT", "TEXT");

    public String format(List<PvVar> vars) {
        if (vars == null || vars.isEmpty()) {
            return "";
        }
        return vars.stream()
                .filter(this::isIncluded)
                .map(this::toLine)
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

    public String formatMerged(List<PvVar> productVars, List<PvVar> calculatorVars) {
        return mergeVars(productVars, calculatorVars).values().stream()
                .map(this::toLine)
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

    private String toLine(PvVar var) {
        return var.getVarCode() + ": " + firstNonBlank(var.getVarName(), var.getName());
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

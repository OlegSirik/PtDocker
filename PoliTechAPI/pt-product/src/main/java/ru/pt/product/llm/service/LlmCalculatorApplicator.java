package ru.pt.product.llm.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmCalculatorApplicator {

    private static final int NEW_VAR_NR_START = 1001;

    public void apply(CalculatorModel target, CalculatorModel source) {
        if (target == null || source == null) {
            return;
        }

        target.setVars(finalizeVars(target.getVars(), source.getVars()));
        if (source.getFormulas() != null) {
            target.setFormulas(new ArrayList<>(source.getFormulas()));
        }
        if (source.getCoefficients() != null) {
            target.setCoefficients(new ArrayList<>(source.getCoefficients()));
        }
        if (source.getLlmText() != null && !source.getLlmText().isBlank()) {
            target.setLlmText(source.getLlmText());
        }

        ensureCoefficientVars(target);
    }

    private List<PvVar> finalizeVars(List<PvVar> requestVars, List<PvVar> responseVars) {
        Map<String, PvVar> requestByCode = indexByVarCode(requestVars);
        List<PvVar> result = new ArrayList<>();
        int nextVarNr = NEW_VAR_NR_START;

        if (responseVars != null) {
            for (PvVar responseVar : responseVars) {
                if (responseVar == null || isBlank(responseVar.getVarCode())) {
                    continue;
                }
                PvVar requestVar = requestByCode.get(responseVar.getVarCode());
                if (requestVar != null) {
                    result.add(copyVar(requestVar));
                    requestByCode.remove(responseVar.getVarCode());
                } else {
                    result.add(createNewVar(responseVar, nextVarNr++));
                }
            }
        }

        result.addAll(requestByCode.values());
        return result;
    }

    private void ensureCoefficientVars(CalculatorModel calculator) {
        if (calculator.getCoefficients() == null || calculator.getCoefficients().isEmpty()) {
            return;
        }
        if (calculator.getVars() == null) {
            calculator.setVars(new ArrayList<>());
        }

        int nextVarNr = calculator.getVars().stream()
                .map(PvVar::getVarNr)
                .filter(nr -> nr != null && !nr.isBlank())
                .mapToInt(nr -> {
                    try {
                        return Integer.parseInt(nr);
                    } catch (NumberFormatException ex) {
                        return NEW_VAR_NR_START - 1;
                    }
                })
                .max()
                .orElse(NEW_VAR_NR_START - 1) + 1;

        for (CoefficientDef coefficient : calculator.getCoefficients()) {
            if (coefficient == null || isBlank(coefficient.getVarCode())) {
                continue;
            }
            boolean exists = calculator.getVars().stream()
                    .anyMatch(v -> coefficient.getVarCode().equals(v.getVarCode()));
            if (exists) {
                continue;
            }

            PvVar stub = new PvVar();
            stub.setVarCode(coefficient.getVarCode());
            stub.setVarName(coefficient.getVarName() != null ? coefficient.getVarName() : coefficient.getVarCode());
            calculator.getVars().add(createNewVar(stub, nextVarNr++));
        }
    }

    private static Map<String, PvVar> indexByVarCode(List<PvVar> vars) {
        Map<String, PvVar> indexed = new LinkedHashMap<>();
        if (vars == null) {
            return indexed;
        }
        for (PvVar var : vars) {
            if (var != null && !isBlank(var.getVarCode()) && !indexed.containsKey(var.getVarCode())) {
                indexed.put(var.getVarCode(), var);
            }
        }
        return indexed;
    }

    private static PvVar createNewVar(PvVar responseVar, int varNr) {
        PvVar created = new PvVar();
        created.setVarDataType(VarDataType.NUMBER);
        created.setVarCode(responseVar.getVarCode());
        created.setVarName(responseVar.getVarCode());
        created.setVarPath(null);
        created.setVarType("VAR");
        created.setVarValue("");
        created.setVarCdm("CALCULATOR");
        created.setVarNr(String.valueOf(varNr));
        created.setId(null);
        created.setParent_id(null);
        created.setVarList(null);
        created.setIsSystem(false);
        created.setIsDeleted(false);
        created.setIsTarifFactor(true);
        created.setIsOptional(true);
        created.setName(null);

        mergeFilledFields(created, responseVar);
        return created;
    }

    private static void mergeFilledFields(PvVar target, PvVar response) {
        if (response.getVarDataType() != null) {
            target.setVarDataType(response.getVarDataType());
        }
        if (!isBlank(response.getVarName())) {
            target.setVarName(response.getVarName());
        } else if (!isBlank(response.getName())) {
            target.setVarName(response.getName());
        }
        if (!isBlank(response.getVarPath())) {
            target.setVarPath(response.getVarPath());
        }
        if (!isBlank(response.getVarType())) {
            target.setVarType(response.getVarType());
        }
        if (response.getVarValue() != null) {
            target.setVarValue(response.getVarValue());
        }
        if (!isBlank(response.getVarCdm())) {
            target.setVarCdm(response.getVarCdm());
        }
        if (!isBlank(response.getVarNr())) {
            target.setVarNr(response.getVarNr());
        }
        if (response.getId() != null) {
            target.setId(response.getId());
        }
        if (response.getParent_id() != null) {
            target.setParent_id(response.getParent_id());
        }
        if (response.getVarList() != null) {
            target.setVarList(response.getVarList());
        }
        if (!isBlank(response.getName())) {
            target.setName(response.getName());
        }
        if (hasExplicitBooleanFields(response)) {
            target.setIsSystem(response.getIsSystem());
            target.setIsDeleted(response.getIsDeleted());
            target.setIsTarifFactor(response.getIsTarifFactor());
            target.setIsOptional(response.getIsOptional());
        }
    }

    private static boolean hasExplicitBooleanFields(PvVar response) {
        return response.getVarDataType() != null
                || !isBlank(response.getVarType())
                || !isBlank(response.getVarCdm())
                || !isBlank(response.getVarPath())
                || response.getId() != null
                || response.getParent_id() != null;
    }

    private static PvVar copyVar(PvVar source) {
        PvVar copy = new PvVar();
        copy.setVarDataType(source.getVarDataType());
        copy.setVarCode(source.getVarCode());
        copy.setVarName(source.getVarName());
        copy.setVarPath(source.getVarPath());
        copy.setVarType(source.getVarType());
        copy.setVarValue(source.getVarValue());
        copy.setVarCdm(source.getVarCdm());
        copy.setVarNr(source.getVarNr());
        copy.setId(source.getId());
        copy.setParent_id(source.getParent_id());
        copy.setVarList(source.getVarList());
        copy.setIsSystem(source.getIsSystem());
        copy.setIsDeleted(source.getIsDeleted());
        copy.setIsTarifFactor(source.getIsTarifFactor());
        copy.setIsOptional(source.getIsOptional());
        copy.setName(source.getName());
        return copy;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

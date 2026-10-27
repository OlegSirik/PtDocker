package ru.pt.product.llm.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.calculator.CalcVarKind;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CalculatorVar;
import ru.pt.api.dto.calculator.CalculatorVarMigration;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.product.VarDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class LlmCalculatorApplicator {

    public void apply(CalculatorModel target, CalculatorModel source) {
        if (target == null || source == null) {
            return;
        }

        target.setVars(finalizeVars(target.getVars(), source.getVars()));
        // Delta mode: omit / empty formulas|coefficients means "leave target as-is".
        if (source.getFormulas() != null && !source.getFormulas().isEmpty()) {
            target.setFormulas(new ArrayList<>(source.getFormulas()));
        }
        if (source.getCoefficients() != null && !source.getCoefficients().isEmpty()) {
            target.setCoefficients(new ArrayList<>(source.getCoefficients()));
        }
        if (source.getLlmText() != null && !source.getLlmText().isBlank()) {
            target.setLlmText(source.getLlmText());
        }

        ensureCoefficientVars(target);
        CalculatorVarMigration.normalizeModel(target);
    }

    private List<CalculatorVar> finalizeVars(List<CalculatorVar> requestVars, List<CalculatorVar> responseVars) {
        Map<String, CalculatorVar> requestByCode = indexByVarCode(requestVars);
        List<CalculatorVar> result = new ArrayList<>();

        if (responseVars != null) {
            for (CalculatorVar responseVar : responseVars) {
                if (responseVar == null || isBlank(responseVar.getVarCode())) {
                    continue;
                }
                CalculatorVar requestVar = requestByCode.get(responseVar.getVarCode());
                if (requestVar != null) {
                    result.add(copyVar(requestVar));
                    requestByCode.remove(responseVar.getVarCode());
                } else {
                    result.add(createNewVar(responseVar));
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

        for (CoefficientDef coefficient : calculator.getCoefficients()) {
            if (coefficient == null || isBlank(coefficient.getVarCode())) {
                continue;
            }

            CalculatorVar existing = findVarByCode(calculator.getVars(), coefficient.getVarCode());
            if (existing != null) {
                applyCoefficientVarShape(existing, coefficient);
            } else {
                calculator.getVars().add(createCoefficientVar(coefficient));
            }
        }
    }

    private static void applyCoefficientVarShape(CalculatorVar var, CoefficientDef coefficient) {
        var.setVarKind(CalcVarKind.COEFFICIENT);
        var.setVarDataType(VarDataType.NUMBER);
        if (var.getVarValue() == null) {
            var.setVarValue("");
        }
        if (isBlank(var.getVarName()) && coefficient.getVarName() != null) {
            var.setVarName(coefficient.getVarName());
        }
    }

    private static CalculatorVar createCoefficientVar(CoefficientDef coefficient) {
        CalculatorVar created = new CalculatorVar(
                coefficient.getVarCode(),
                coefficient.getVarName() != null ? coefficient.getVarName() : coefficient.getVarCode(),
                CalcVarKind.COEFFICIENT,
                VarDataType.NUMBER);
        created.setVarValue("");
        return created;
    }

    private static CalculatorVar findVarByCode(List<CalculatorVar> vars, String varCode) {
        if (vars == null) {
            return null;
        }
        for (CalculatorVar var : vars) {
            if (var != null && varCode.equals(var.getVarCode())) {
                return var;
            }
        }
        return null;
    }

    private static Map<String, CalculatorVar> indexByVarCode(List<CalculatorVar> vars) {
        Map<String, CalculatorVar> indexed = new LinkedHashMap<>();
        if (vars == null) {
            return indexed;
        }
        for (CalculatorVar var : vars) {
            if (var != null && !isBlank(var.getVarCode()) && !indexed.containsKey(var.getVarCode())) {
                indexed.put(var.getVarCode(), var);
            }
        }
        return indexed;
    }

    private static CalculatorVar createNewVar(CalculatorVar responseVar) {
        CalcVarKind varKind = responseVar.getVarKind();
        if (varKind == null) {
            varKind = CalcVarKind.parse(responseVar.getVarType());
        }
        if (varKind == null || varKind == CalcVarKind.POLICY || varKind == CalcVarKind.COVER) {
            varKind = CalcVarKind.VAR;
        }
        if (varKind != CalcVarKind.CONST && varKind != CalcVarKind.COEFFICIENT) {
            varKind = CalcVarKind.VAR;
        }

        CalculatorVar created = new CalculatorVar(
                responseVar.getVarCode(),
                firstNonBlank(responseVar.getVarName(), responseVar.getVarCode()),
                varKind,
                responseVar.getVarDataType() != null ? responseVar.getVarDataType() : VarDataType.NUMBER);
        if (varKind == CalcVarKind.CONST && responseVar.getVarValue() != null) {
            created.setVarValue(responseVar.getVarValue());
        } else {
            created.setVarValue("");
        }
        return created;
    }

    private static CalculatorVar copyVar(CalculatorVar source) {
        CalculatorVar copy = new CalculatorVar(
                source.getVarCode(),
                source.getVarName(),
                source.getVarKind(),
                source.getVarDataType());
        copy.setVarValue(source.getVarValue());
        copy.setSystem(source.getSystem());
        return copy;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        return b;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

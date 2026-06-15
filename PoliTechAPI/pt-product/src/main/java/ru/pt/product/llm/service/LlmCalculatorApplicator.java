package ru.pt.product.llm.service;

import org.springframework.stereotype.Component;
import ru.pt.api.dto.calculator.CalculatorModel;
import ru.pt.api.dto.calculator.CoefficientDef;
import ru.pt.api.dto.calculator.FormulaDef;
import ru.pt.api.dto.calculator.FormulaLine;
import ru.pt.api.dto.llm.LlmCalculatorDraft;
import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class LlmCalculatorApplicator {

    public void apply(CalculatorModel calculator, LlmCalculatorDraft draft) {
        if (calculator == null || draft == null) {
            return;
        }
        if (draft.getFormulas() != null && !draft.getFormulas().isEmpty()) {
            applyFormulas(calculator, draft.getFormulas());
        }
        if (draft.getCoefficients() != null && !draft.getCoefficients().isEmpty()) {
            applyCoefficients(calculator, draft.getCoefficients());
        }
    }

    private void applyFormulas(CalculatorModel calculator, List<FormulaDef> incomingFormulas) {
        List<FormulaLine> lines = new ArrayList<>();
        for (FormulaDef formula : incomingFormulas) {
            if (formula.getLines() != null) {
                lines.addAll(formula.getLines());
            }
        }
        if (lines.isEmpty()) {
            return;
        }

        lines.sort(Comparator.comparing(line -> line.getNr() != null ? line.getNr() : 0L));

        if (calculator.getFormulas() == null) {
            calculator.setFormulas(new ArrayList<>());
        }

        if (calculator.getFormulas().isEmpty()) {
            FormulaDef first = incomingFormulas.getFirst();
            FormulaDef created = new FormulaDef();
            created.setVarCode(first.getVarCode());
            created.setVarName(first.getVarName() != null ? first.getVarName() : first.getVarCode());
            created.setLines(lines);
            calculator.getFormulas().add(created);
            return;
        }

        FormulaDef current = calculator.getFormulas().getFirst();
        current.setLines(lines);
    }

    private void applyCoefficients(CalculatorModel calculator, List<CoefficientDef> incomingCoefficients) {
        if (calculator.getCoefficients() == null) {
            calculator.setCoefficients(new ArrayList<>());
        }
        if (calculator.getVars() == null) {
            calculator.setVars(new ArrayList<>());
        }

        for (CoefficientDef incoming : incomingCoefficients) {
            if (incoming == null || incoming.getVarCode() == null || incoming.getVarCode().isBlank()) {
                continue;
            }

            upsertCoefficientVar(calculator, incoming);

            int existingIndex = -1;
            for (int i = 0; i < calculator.getCoefficients().size(); i++) {
                if (incoming.getVarCode().equals(calculator.getCoefficients().get(i).getVarCode())) {
                    existingIndex = i;
                    break;
                }
            }
            if (existingIndex >= 0) {
                calculator.getCoefficients().set(existingIndex, incoming);
            } else {
                calculator.getCoefficients().add(incoming);
            }
        }
    }

    private void upsertCoefficientVar(CalculatorModel calculator, CoefficientDef coefficient) {
        PvVar existing = calculator.getVars().stream()
                .filter(v -> coefficient.getVarCode().equals(v.getVarCode()))
                .findFirst()
                .orElse(null);

        String varName = coefficient.getVarName() != null ? coefficient.getVarName() : coefficient.getVarCode();
        if (existing != null) {
            existing.setVarName(varName);
            existing.setVarType("COEFFICIENT");
            return;
        }

        PvVar created = new PvVar();
        created.setVarCode(coefficient.getVarCode());
        created.setVarName(varName);
        created.setVarPath("");
        created.setVarType("COEFFICIENT");
        created.setVarValue("");
        created.setVarDataType(VarDataType.NUMBER);
        created.setVarCdm("CALCULATOR");
        created.setVarNr("10000");
        created.setIsOptional(true);
        calculator.getVars().add(created);
    }
}

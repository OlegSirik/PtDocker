package ru.pt.api.dto.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.pt.api.dto.product.VarDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Tolerant read: старый элемент {@code PvVar} / {@code kind} в calculator JSON → {@link CalculatorVar}.
 * Product-only типы (IN/MAGIC/OBJECT/TEXT без cover-кода) отбрасываются.
 */
public final class CalculatorVarMigration {

    private static final Logger log = LoggerFactory.getLogger(CalculatorVarMigration.class);

    private static final Pattern COVER_CODE = Pattern.compile(
            "^co_.+_(sumInsured|premium|deductibleNr)(_\\d+)?$",
            Pattern.CASE_INSENSITIVE);

    private CalculatorVarMigration() {
    }

    public static void normalizeModel(CalculatorModel model) {
        if (model == null) {
            return;
        }
        model.setVars(normalizeList(model.getVars()));
    }

    public static List<CalculatorVar> normalizeList(List<CalculatorVar> raw) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, CalculatorVar> byCode = new LinkedHashMap<>();
        for (CalculatorVar in : raw) {
            CalculatorVar migrated = migrateOne(in);
            if (migrated == null || migrated.getVarCode() == null || migrated.getVarCode().isBlank()) {
                continue;
            }
            String code = migrated.getVarCode().trim();
            migrated.setVarCode(code);
            byCode.putIfAbsent(code, migrated);
        }
        return new ArrayList<>(byCode.values());
    }

    public static CalculatorVar migrateOne(CalculatorVar in) {
        if (in == null) {
            return null;
        }
        String code = in.getVarCode() != null ? in.getVarCode().trim() : null;
        if (code == null || code.isBlank()) {
            return null;
        }

        CalcVarKind varKind = in.getVarKind();
        if (varKind == null) {
            varKind = CalcVarKind.parse(in.getVarType());
        }
        if (varKind == null && isCoverCode(code)) {
            varKind = CalcVarKind.COVER;
        }

        String legacyType = in.getVarType() != null
                ? in.getVarType().trim().toUpperCase(Locale.ROOT)
                : "";

        // Product overlay accidentally persisted — drop
        if (varKind == null || isProductOnlyType(legacyType, varKind, code)) {
            if (!isCoverCode(code)) {
                log.debug("Dropping non-calculator var from calculator JSON: code={}, type={}",
                        code, legacyType);
                return null;
            }
            varKind = CalcVarKind.COVER;
        }

        CalculatorVar out = new CalculatorVar();
        out.setVarCode(code);
        out.setVarName(in.getVarName() != null && !in.getVarName().isBlank() ? in.getVarName() : code);
        out.setVarKind(varKind);
        out.setVarDataType(in.getVarDataType() != null ? in.getVarDataType() : defaultDataType(varKind));
        if (varKind == CalcVarKind.CONST) {
            out.setVarValue(in.getVarValue());
        }
        if (varKind == CalcVarKind.COVER || Boolean.TRUE.equals(in.getSystem())) {
            out.setSystem(true);
        }
        // clear legacy noise
        out.setVarCdm(null);
        out.setVarType(null);
        return out;
    }

    public static boolean isCoverCode(String varCode) {
        return varCode != null && COVER_CODE.matcher(varCode.trim()).matches();
    }

    private static boolean isProductOnlyType(String legacyType, CalcVarKind varKind, String code) {
        if (varKind == CalcVarKind.POLICY
                || varKind == CalcVarKind.CONST
                || varKind == CalcVarKind.COEFFICIENT
                || varKind == CalcVarKind.VAR
                || varKind == CalcVarKind.COVER) {
            return false;
        }
        if ("IN".equals(legacyType) || "MAGIC".equals(legacyType)
                || "OBJECT".equals(legacyType) || "TEXT".equals(legacyType)) {
            return !isCoverCode(code);
        }
        return varKind == null;
    }

    private static VarDataType defaultDataType(CalcVarKind varKind) {
        return varKind == CalcVarKind.COVER || varKind == CalcVarKind.COEFFICIENT || varKind == CalcVarKind.CONST
                ? VarDataType.NUMBER
                : VarDataType.STRING;
    }
}

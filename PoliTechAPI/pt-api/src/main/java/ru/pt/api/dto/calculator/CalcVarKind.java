package ru.pt.api.dto.calculator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Тип переменной словаря калькулятора (изолирован от product {@code varType}).
 */
public enum CalcVarKind {
    POLICY,
    CONST,
    COEFFICIENT,
    VAR,
    COVER;

    @JsonValue
    public String json() {
        return name();
    }

    @JsonCreator
    public static CalcVarKind fromJson(String raw) {
        return parse(raw);
    }

    /** Неизвестное → {@code null} (не exception). */
    public static CalcVarKind parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT);
        for (CalcVarKind k : values()) {
            if (k.name().equals(n)) {
                return k;
            }
        }
        // legacy aliases
        if ("OUT".equals(n) || "CALC".equals(n)) {
            return VAR;
        }
        return null;
    }
}

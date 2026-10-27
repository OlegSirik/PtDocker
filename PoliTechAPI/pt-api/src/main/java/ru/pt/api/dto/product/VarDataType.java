package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Тип значения переменной. Канон: {@code STRING}, {@code NUMBER}, {@code OBJECT}.
 * Неизвестные значения (DATE/TIME/…) при чтении → {@code STRING}.
 */
public enum VarDataType {
    STRING,
    NUMBER,
    OBJECT;

    @JsonValue
    public String json() {
        return name();
    }

    @JsonCreator
    public static VarDataType fromJson(String raw) {
        return parse(raw);
    }

    /** Неизвестное / пустое → {@link #STRING}. */
    public static VarDataType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return STRING;
        }
        String n = raw.trim().toUpperCase(Locale.ROOT);
        for (VarDataType t : values()) {
            if (t.name().equals(n)) {
                return t;
            }
        }
        return STRING;
    }
}

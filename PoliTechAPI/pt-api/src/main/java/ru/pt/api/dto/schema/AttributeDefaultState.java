package ru.pt.api.dto.schema;

/**
 * Статус атрибута в {@code mt_attribute_def.default_state}.
 */
public final class AttributeDefaultState {

    public static final String ENABLED = "ENABLED";
    public static final String DISABLED = "DISABLED";

    private AttributeDefaultState() {
    }

    public static boolean isDisabled(String value) {
        return DISABLED.equalsIgnoreCase(normalize(value));
    }

    public static boolean isEnabled(String value) {
        return !isDisabled(value);
    }

    /** Пустое / неизвестное → ENABLED. */
    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return ENABLED;
        }
        String v = value.trim().toUpperCase();
        if (DISABLED.equals(v)) {
            return DISABLED;
        }
        return ENABLED;
    }
}

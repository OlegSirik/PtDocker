package ru.pt.rules.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Преобразование значений из map контекста полиса для CEL-функций {@code num()} / {@code str()}.
 */
public final class CelVariableHelpers {

    private CelVariableHelpers() {
    }

    public static BigDecimal num(Map<String, Object> ctx, String key) {
        if (ctx == null || key == null) {
            return null;
        }
        Object v = ctx.get(key);
        if (v == null) {
            return null;
        }
        if (v instanceof BigDecimal bd) {
            return bd;
        }
        if (v instanceof Integer i) {
            return BigDecimal.valueOf(i);
        }
        if (v instanceof Long l) {
            return BigDecimal.valueOf(l);
        }
        if (v instanceof Double d) {
            return BigDecimal.valueOf(d);
        }
        if (v instanceof Float f) {
            return BigDecimal.valueOf(f.doubleValue());
        }
        return new BigDecimal(v.toString());
    }

    public static String str(Map<String, Object> ctx, String key) {
        if (ctx == null || key == null) {
            return null;
        }
        Object v = ctx.get(key);
        return v == null ? null : String.valueOf(v);
    }

    /** CEL activation: null → protobuf null, число → long/double. */
    static Object numForCel(Map<String, Object> ctx, String key) {
        BigDecimal bd = num(ctx, key);
        if (bd == null) {
            return com.google.protobuf.NullValue.NULL_VALUE;
        }
        try {
            return bd.longValueExact();
        } catch (ArithmeticException ex) {
            return bd.doubleValue();
        }
    }

    static Object strForCel(Map<String, Object> ctx, String key) {
        String s = str(ctx, key);
        return s == null ? com.google.protobuf.NullValue.NULL_VALUE : s;
    }
}

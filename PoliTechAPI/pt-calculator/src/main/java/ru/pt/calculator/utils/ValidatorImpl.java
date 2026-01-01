package ru.pt.calculator.utils;

import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


public class ValidatorImpl {


    private static boolean checkString(String type, String v1, String v2) {

        switch (type) {
            case "NOT_NULL": return v1 != null && !v1.isEmpty();
            case "=": return Objects.equals(v1, v2);
            case "!=": return !Objects.equals(v1, v2);
            case "MATCHES_REGEX": return v1 != null && v2 != null && v1.matches(v2);
            case "IN_LIST":
                if (v2 == null) return false;
                String[] items = v2.split(",");
                for (String item : items) {
                    if (Objects.equals(v1, item.trim())) {
                        return true;
                    }
                }
                return false;
            default: return false;
        }
    }

    private static boolean checkNumber(String type, String s1, String s2) {

        switch (type) {
            case "=": return Double.parseDouble(s1) == Double.parseDouble(s2);
            case "!=": return Double.parseDouble(s1) != Double.parseDouble(s2);
            case ">": return Double.parseDouble(s1) > Double.parseDouble(s2);
            case "<": return Double.parseDouble(s1) < Double.parseDouble(s2);
            case ">=": return Double.parseDouble(s1) >= Double.parseDouble(s2);
            case "<=": return Double.parseDouble(s1) <= Double.parseDouble(s2);
            case "RANGE":
                String[] rightParts = s2.split("-");

                if (Double.parseDouble(s1) >= Double.parseDouble(rightParts[0].trim()) && Double.parseDouble(s1) <= Double.parseDouble(rightParts[1].trim())) {
                    return true;
                }

                return false;
            default: return false;
        }
    }

    private static boolean checkNumber(String type, BigDecimal s1, BigDecimal s2) {

        switch (type) {
            case "=": return s1.compareTo(s2) == 0;
            case "!=": return s1.compareTo(s2) != 0;
            case ">": return s1.compareTo(s2) > 0;
            case "<": return s1.compareTo(s2) < 0;
            case ">=": return s1.compareTo(s2) >= 0;
            case "<=": return s1.compareTo(s2) <= 0;
            default: return false;
        }
    }

    public static boolean validate(List<PvVar> dataMap, String leftKey, String rightKey, String rightValue, String ruleType) {
        try {
            PvVar leftVarDef = dataMap.stream().filter(v -> v.getVarCode().equals(leftKey)).findFirst().orElse(null);
            PvVar rightVarDef = dataMap.stream().filter(v -> v.getVarCode().equals(rightKey)).findFirst().orElse(null);
            if (rightVarDef != null) {
                if (! leftVarDef.getVarType().equalsIgnoreCase(rightVarDef.getVarType())) {
                    return false;
                }
            }
            if ( rightVarDef == null) {
                rightVarDef = new PvVar(rightKey, "","",leftVarDef.getVarType(), rightValue, leftVarDef.getVarDataType());
            }

            if (leftVarDef == null || rightVarDef == null) return false;
            if (leftVarDef.getVarDataType() == VarDataType.NUMBER) {
                return checkNumber(ruleType, leftVarDef.getVarValue(), rightVarDef.getVarValue());
            }
            if (leftVarDef.getVarDataType() == VarDataType.STRING) {
                return checkString(ruleType, leftVarDef.getVarValue(), rightVarDef.getVarValue());
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validate(VariableContext ctx, String leftKey, String rightKey, String rightValue, String ruleType) {
        try {
            PvVarDefinition leftVarDef = ctx.getDefinition(leftKey);
            PvVarDefinition rightVarDef = ctx.getDefinition(rightKey);

            if (leftVarDef == null ) return false;
            if (rightVarDef == null && rightValue == null) return false;

            if (rightVarDef != null) {
                if (leftVarDef.getType() != rightVarDef.getType()) {
                    return false;
                }
            }

            if ( leftVarDef.getType() == PvVarDefinition.Type.NUMBER ) {
                BigDecimal leftVal = ctx.getDecimal(leftKey);
                BigDecimal rightVal = null;
                if ( rightKey != null ) { rightVal = ctx.getDecimal(rightKey); }
                else { rightVal = new BigDecimal(rightValue); } 
                return checkNumber(ruleType, leftVal, rightVal);
            } else {
                String leftVal = ctx.getString(leftKey);
                String rightVal = null;
                if ( rightKey != null ) { rightVal = ctx.getString(rightKey); }
                else { rightVal = rightValue; }
                return checkString(ruleType, leftVal, rightVal);
            }

        } catch (Exception e) {
            return false;
        }
    }

}

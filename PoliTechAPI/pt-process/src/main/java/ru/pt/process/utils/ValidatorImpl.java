package ru.pt.process.utils;

import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.api.dto.product.VarDataType;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ValidatorImpl {


    private static boolean checkString(String type, String v1, String v2) {

        switch (type) {
            case "NOT_NULL":
                return v1 != null && !v1.isEmpty();
            case "=":
                return Objects.equals(v1, v2);
            case "!=":
                return !Objects.equals(v1, v2);
            case "MATCHES_REGEX":
                return v1 != null && v2 != null && v1.matches(v2);
            case "IN_LIST":
                if (v2 == null) return false;
                String[] items = v2.split(",");
                for (String item : items) {
                    if (Objects.equals(v1, item.trim())) {
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }

    private static boolean checkNumber(String type, String s1, String s2) {

        switch (type) {
            case "=":
                return Double.parseDouble(s1) == Double.parseDouble(s2);
            case "!=":
                return Double.parseDouble(s1) != Double.parseDouble(s2);
            case ">":
                return Double.parseDouble(s1) > Double.parseDouble(s2);
            case "<":
                return Double.parseDouble(s1) < Double.parseDouble(s2);
            case ">=":
                return Double.parseDouble(s1) >= Double.parseDouble(s2);
            case "<=":
                return Double.parseDouble(s1) <= Double.parseDouble(s2);
            case "RANGE":
                String[] rightParts = s2.split("-");

                if (Double.parseDouble(s1) >= Double.parseDouble(rightParts[0].trim()) && Double.parseDouble(s1) <= Double.parseDouble(rightParts[1].trim())) {
                    return true;
                }

                return false;
            default:
                return false;
        }
    }

    public static boolean validate(
            Map<String, PvVar> dataMap,
            ValidatorRule validatorRule
    ) {
        String leftKey = validatorRule.getKeyLeft();
        String rightKey = validatorRule.getKeyRight();
        String rightValue = validatorRule.getValueRight();
        String ruleType = validatorRule.getRuleType();
        try {
            // TODO медленно
            PvVar leftVarDef = dataMap.get(leftKey);
            PvVar rightVarDef = dataMap.get(rightKey);
            if (rightVarDef != null) {
                if (!leftVarDef.getVarType().equalsIgnoreCase(rightVarDef.getVarType())) {
                    return false;
                }
            }
            if (rightVarDef == null) {
                rightVarDef = new PvVar(rightKey, "", "", leftVarDef.getVarType(), rightValue, leftVarDef.getVarDataType());
            }

            if (leftVarDef == null || rightVarDef == null){
                return false;
            }
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



    private static boolean checkBigD(String type, BigDecimal s1, BigDecimal s2) {
    
        switch (type) {
            case "=": {
                return s1.compareTo(s2) == 0;
            }
            case "!=": {
                return s1.compareTo(s2) != 0;
            }
            case ">": {
                return s1.compareTo(s2) > 0;
            }
            case "<": {
                return s1.compareTo(s2) < 0;
            }
            case ">=": {
                return s1.compareTo(s2) >= 0;
            }
            case "<=": {
                return s1.compareTo(s2) <= 0;
            }
            default:
                throw new IllegalArgumentException("Unknown operator: " + type);
        }
    }
    
    public static boolean validate(VariableContext ctx, ValidatorRule rule) {
        String leftKey = rule.getKeyLeft();
        String rightKey = rule.getKeyRight();
        String rightValue = rule.getValueRight();
        String ruleType = rule.getRuleType();
    
        try {
            PvVarDefinition leftDef = ctx.getDefinition(leftKey);
            if (leftDef == null) return false;
    
            PvVarDefinition rightDef = ctx.getDefinition(rightKey);
    
            Object rightVal;
    
            if (rightDef != null) {
                // проверяем совпадение типа
                if (leftDef.getType() != rightDef.getType()) {
                    return false;
                }
                rightVal = ctx.get(rightKey);
            } else {
                // если нет переменной в контексте, используем константу
                if (leftDef.getType() == PvVarDefinition.Type.NUMBER) {
                    rightVal = new BigDecimal(rightValue.trim());
                } else {
                    rightVal = rightValue;
                }
            }
    
            Object leftVal = ctx.get(leftKey);
            if (leftVal == null || rightVal == null) return false;
    
            switch (leftDef.getType()) {
                case NUMBER -> {
                    BigDecimal leftNum = (BigDecimal) leftVal;
                    BigDecimal rightNum = (BigDecimal) rightVal;
                    return checkBigD(ruleType, leftNum, rightNum);
                }
                case STRING -> {
                    String leftStr = leftVal.toString();
                    String rightStr = rightVal.toString();
                    return checkString(ruleType, leftStr, rightStr);
                }
                default -> {
                    return false;
                }
            }
    
        } catch (Exception e) {
            return false;
        }
    }
    
}

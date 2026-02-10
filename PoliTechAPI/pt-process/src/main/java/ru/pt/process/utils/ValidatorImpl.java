package ru.pt.process.utils;

import ru.pt.api.dto.product.ValidatorRule;
import ru.pt.domain.model.PvVarDefinition;
import ru.pt.domain.model.VariableContext;
import ru.pt.process.utils.validators.StandardValidators;

import java.math.BigDecimal;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidatorImpl.class);
    /*
    * ToDoc
    * Проверки строк:
    *   1. Стандартные проверки, типа EMAIL, PHONE, INN, SNILS, PASSPORT. Проверяется соответсвие шаблону
    *   2. NOT_NULL параметр должен быть заполнен.
    *   3. = и != равенство двух срок.
    *   4. IN_LIST ( В списке ). Атрибут может быть пустым. Если он не пустой, то должен соответствовать одному из фиксированных значений.
    *       Обязательность проверяется отдельной проверкой.
    *   5. HES_REGEX - проверка на соответсвие шаблону.
    * 
    * Проверка числовых значений:
    *   1. Все варианты =,!=, <,> 
    *   2. IN_RANGE - значение находится в диапазоне, включая граничные знаечния
    */
   
    private static boolean checkString(String type, String v1, String v2) {
        // First check standard validators (email, phone, etc.)
        if (type.matches("^(EMAIL|PHONE|PHONE_RU|PHONE_INTERNATIONAL|INN|SNILS|PASSPORT)$")) {
            return StandardValidators.validate(type, v1);
        }

        // Then check other string validations
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
                if (v1 == null || v1.isEmpty()) { return true;}

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
        String rightValue = "";
        String ruleType = rule.getRuleType();
        if (rule.isKeyRightCustomValue()) {
            rightValue = rightKey;
        }

        try {
            PvVarDefinition leftDef = ctx.getDefinition(leftKey);
            if (leftDef == null) {
                LOGGER.trace("Validation skipped: left definition missing for key {}", leftKey);
                return false;
            }
    
            PvVarDefinition rightDef = ctx.getDefinition(rightKey);
    
            Object rightVal;
    
            if (rightDef != null) {
                // проверяем совпадение типа
                if (leftDef.getType() != rightDef.getType()) {
                    LOGGER.trace(
                        "Validation failed: type mismatch for keys {} and {} ({} vs {})",
                        leftKey,
                        rightKey,
                        leftDef.getType(),
                        rightDef.getType()
                    );
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
            if (leftVal == null || rightVal == null) {
                LOGGER.trace(
                    "Validation failed: null value for keys {} or {}",
                    leftKey,
                    rightKey
                );
                return false;
            }
    
            switch (leftDef.getType()) {
                case NUMBER -> {
                    BigDecimal leftNum = (BigDecimal) leftVal;
                    BigDecimal rightNum = (BigDecimal) rightVal;
                    return checkBigD(ruleType, leftNum, rightNum);
                }
                case STRING -> {
                    String rightStr = rightVal.toString();
                    String leftStr = leftVal.toString();
                    return checkString(ruleType, leftStr, rightStr);
                }
                default -> {
                    LOGGER.trace("Validation failed: unsupported type {}", leftDef.getType());
                    return false;
                }
            }
    
        } catch (Exception e) {
            LOGGER.trace("Validation exception for keys {} and {} with rule {}", leftKey, rightKey, ruleType, e);
            return false;
        }
    }
    
}

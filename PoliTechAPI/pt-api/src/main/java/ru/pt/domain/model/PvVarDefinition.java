package ru.pt.domain.model;

public final class PvVarDefinition {

    public enum Type { STRING, NUMBER }

    public enum VarScope { DOMAIN, CALCULATOR }
    public enum VarSourceType { IN, MAGIC,CONST, COEFFICIENT, VAR, CALC }
    public enum GroupFunctionName {
        HZ("hz"),
        SUM("sum"),
        AVG("avg"),
        MIN("min"),
        MAX("max"),
        COUNT("count");

        private final String value;
        
        GroupFunctionName(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static GroupFunctionName fromString(String str) {
            if (str == null) return null;
            for (GroupFunctionName fn : values()) {
                if (fn.value.equalsIgnoreCase(str)) {
                    return fn;
                }
            }
            return null;
        }
    }

    private final String code;
    private final String jsonPath;
    private final Type type;
    private final VarScope scope;
    private final VarSourceType sourceType;
    private final GroupFunctionName groupFunctionName;

    public PvVarDefinition(String code, String jsonPath, Type type, String strSourceType) {
        //this.code = code;
        /*
        Функции к массивам не поддерживаются. Поэтому если jsonpath заканчивается на () - это функция.
        Тогда берем функцию из jsonpath и убираем ее из jsonpath.
        Например:
        jsonpath = "$.person[].count()"
        funcName = "count"
        jsonpath = "$.person"
        groupFunctionName = GroupFunctionName.COUNT
        */
/*  
        this.type = type;
        this.scope = VarScope.DOMAIN;
        this.sourceType = VarSourceType.valueOf(strSourceType);
        if (jsonPath != null && jsonPath.endsWith("()")) {
            String funcName = jsonPath.substring(jsonPath.lastIndexOf('.') + 1, jsonPath.length() - 2);
            GroupFunctionName parsed = GroupFunctionName.fromString(funcName);
            this.groupFunctionName = parsed != null ? parsed : GroupFunctionName.HZ;
            this.jsonPath = jsonPath.substring(0, jsonPath.lastIndexOf('.'));
        } else {
            this.groupFunctionName = GroupFunctionName.HZ;
            this.jsonPath = jsonPath;
        }
*/
        this(code, jsonPath, type, VarScope.DOMAIN, VarSourceType.valueOf(strSourceType));
    }

    // Используется в калькуляторе для доп атрибутов
    public PvVarDefinition(String code, String jsonPath, Type type, VarScope scope, VarSourceType sourceType) {
        this.code = code;
        this.type = type;
        this.scope = scope;
        this.sourceType = sourceType;
        if (jsonPath != null && jsonPath.endsWith("()")) {
            String funcName = jsonPath.substring(jsonPath.lastIndexOf('.') + 1, jsonPath.length() - 2);
            GroupFunctionName parsed = GroupFunctionName.fromString(funcName);
            this.groupFunctionName = parsed != null ? parsed : GroupFunctionName.HZ;
            this.jsonPath = jsonPath.substring(0, jsonPath.lastIndexOf('.'));
        } else {
            this.groupFunctionName = GroupFunctionName.HZ;
            this.jsonPath = jsonPath;
        }
        
    }

    // getters only
    public String getCode() { return code; }
    public String getJsonPath() { return jsonPath; }
    public Type getType() { return type; }
    public VarScope getScope() { return scope; }
    public VarSourceType getSourceType() { return sourceType; }
    public GroupFunctionName getGroupFunctionName() { return groupFunctionName; }
    public String getGroupFunctionNameString() { 
        return groupFunctionName != null ? groupFunctionName.getValue() : null; 
    }
}

package ru.pt.domain.model;

import ru.pt.api.dto.product.PvVar;
import ru.pt.api.dto.product.VarDataType;

public final class PvVarDefinition {

    public enum Type { STRING, NUMBER }
    public enum VarScope { DOMAIN, CALCULATOR }
    public enum VarSourceType { IN, MAGIC,CONST, COEFFICIENT, VAR, CALC, TEXT }
    public enum GroupFunctionName {
        HZ("hz"),
        SUM("sum"),
        AVG("avg"),
        MIN("min"),
        MAX("max"),
        COUNT("count"),
        FIRST("first");

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
    private String textTemplate = "";

//    public PvVarDefinition(String code, String jsonPath, Type type, String strSourceType) {
//        this(code, jsonPath, type, VarScope.DOMAIN, VarSourceType.valueOf(strSourceType));
//    }

    // Используется в калькуляторе для доп атрибутов
    public PvVarDefinition(String code, String jsonPath, Type type, VarScope scope, VarSourceType sourceType, String varValue ) {
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
            this.groupFunctionName = null; //GroupFunctionName.HZ;
            this.jsonPath = jsonPath;
        }
        
        if ( sourceType == VarSourceType.TEXT ) {
            this.textTemplate = varValue;
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
    public String getTemplate() { return this.textTemplate; }
    /**
     * Factory method to create PvVarDefinition from PvVar
     * @param pvVar the PvVar to convert
     * @return a new PvVarDefinition instance
     */
    public static PvVarDefinition fromPvVar(PvVar pvVar) {
        Type type;
        switch (pvVar.getVarDataType()) {
            case NUMBER:
                type = Type.NUMBER;
                break;
            case STRING:
            default:
                type = Type.STRING;
                break;
        }
        return new PvVarDefinition(
            pvVar.getVarCode(),
            pvVar.getVarPath(),
            type,
            VarScope.DOMAIN,
            VarSourceType.valueOf(pvVar.getVarType()),
            pvVar.getVarValue()
        );
    }
    
}

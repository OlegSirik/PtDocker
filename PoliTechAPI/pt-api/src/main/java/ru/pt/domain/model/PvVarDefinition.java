package ru.pt.domain.model;

public final class PvVarDefinition {

    public enum Type { STRING, NUMBER }

    public enum VarScope { DOMAIN, CALCULATOR }
    public enum VarSourceType { IN, MAGIC,CONST, COEFFICIENT, VAR, CALC }

    private final String code;
    private final String jsonPath;
    private final Type type;
    private final VarScope scope;
    private final VarSourceType sourceType;

    public PvVarDefinition(String code, String jsonPath, Type type) {
        this.code = code;
        this.jsonPath = jsonPath;
        this.type = type;
        this.scope = VarScope.DOMAIN;
        this.sourceType = VarSourceType.IN;
    }
    public PvVarDefinition(String code, String jsonPath, Type type, VarScope scope, VarSourceType sourceType) {
        this.code = code;
        this.jsonPath = jsonPath;
        this.type = type;
        this.scope = scope;
        this.sourceType = sourceType;
    }

    // getters only
    public String getCode() { return code; }
    public String getJsonPath() { return jsonPath; }
    public Type getType() { return type; }
    public VarScope getScope() { return scope; }
    public VarSourceType getSourceType() { return sourceType; }
}

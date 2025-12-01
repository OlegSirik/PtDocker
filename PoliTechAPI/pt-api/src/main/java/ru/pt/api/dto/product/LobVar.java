package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
public class LobVar {

    @Getter
    @JsonProperty("varDataType")
    private VarDataType varDataType;

    // Getters and Setters
    @Getter
    @JsonProperty("varCode")
    private String varCode;

    @Getter
    @JsonProperty("varName")
    private String varName;

    @Getter
    @JsonProperty("varPath")
    private String varPath;

    @Getter
    @JsonProperty("varType")
    private String varType;

    @JsonProperty("varValue")
    private String varValue = "";

    // Constructors
    public LobVar() {
    }

    public LobVar(String varCode, String varName, String varPath, String varType, VarDataType varDataType) {
        this.varCode = varCode;
        this.varName = varName;
        this.varPath = varPath;
        this.varType = varType;
        this.varDataType = varDataType;
    }

    public LobVar(String varCode, String varName, String varPath, String varType, String varValue, VarDataType varDataType) {
        this.varCode = varCode;
        this.varName = varName;
        this.varPath = varPath;
        this.varType = varType;
        this.varValue = varValue;
        this.varDataType = varDataType;
    }

    public String getVarValue() {
        if (varValue == null) varValue = "";
        return varValue;
    }
}

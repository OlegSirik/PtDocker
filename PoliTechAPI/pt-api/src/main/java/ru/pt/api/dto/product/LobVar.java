package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class LobVar {

    @JsonProperty("varDataType")
    private VarDataType varDataType;
    @JsonProperty("varCode")
    private String varCode;

    @JsonProperty("varName")
    private String varName;

    @JsonProperty("varPath")
    private String varPath;

    @JsonProperty("varType")
    private String varType;

    @JsonProperty("varValue")
    private String varValue = "";
    
    @JsonProperty("varCdm")
    private String varCdm;

    @JsonProperty("varNr")
    private String varNr;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("parent_id")
    private Long parent_id;

    @JsonProperty("varList")
    private String varList;

    @JsonProperty("isSystem")
    private boolean isSystem;

    @JsonProperty("isDeleted")
    private boolean isDeleted;

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

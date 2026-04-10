package ru.pt.api.dto.product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LobCoefficientDef {
    @JsonProperty("varCode")
    private String varCode;

    @JsonProperty("varName")
    private String varName;

    @JsonProperty("altVarCode")
    private String altVarCode;

    @JsonProperty("altVarValue")
    private BigDecimal altVarValue;
    
    @JsonProperty("columns")
    private List<LobCoefficientColumn> columns = new ArrayList<>();
}

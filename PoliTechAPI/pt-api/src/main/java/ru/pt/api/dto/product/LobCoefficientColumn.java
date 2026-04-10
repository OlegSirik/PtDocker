package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LobCoefficientColumn {
    @JsonProperty("nr")
    private Integer nr;

    @JsonProperty("varCode")
    private String varCode;

    @JsonProperty("varDataType")
    private String varDataType;

    @JsonProperty("conditionOperator")
    private String conditionOperator;
    
    @JsonProperty("sortOrder")
    private String sortOrder;
}

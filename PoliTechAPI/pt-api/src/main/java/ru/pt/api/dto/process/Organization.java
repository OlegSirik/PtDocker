package ru.pt.api.dto.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Organization {

    @JsonProperty("inn")
    private String inn;

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("shortName")
    private String shortName;

    @JsonProperty("legalForm")
    private String legalForm;

    @JsonProperty("kpp")
    private String kpp;

    @JsonProperty("ogrn")
    private String ogrn;

    @JsonProperty("okpo")
    private String okpo;

    // Additional attributes
    private Map<String, Object> additionalAttributes;


    // Constructors
    public Organization() {}

}

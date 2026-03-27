package ru.pt.api.dto.process;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Address {
    @JsonProperty("isPrimary")
    private boolean isPrimary;

    @JsonProperty("typeCode")
    private String typeCode;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("region")
    private String region;

    @JsonProperty("city")
    private String city;

    @JsonProperty("street")
    private String street;

    @JsonProperty("house")
    private String house;

    @JsonProperty("building")
    private String building;

    @JsonProperty("flat")
    private String flat;

    @JsonProperty("room")
    private String room;

    @JsonProperty("zipCode")
    private String zipCode;

    @JsonProperty("kladrId")
    private String kladrId;

    @JsonProperty("fiasId")
    private String fiasId;

    @JsonProperty("addressStr")
    private String addressStr;

    @JsonProperty("addressStrEn")
    private String addressStrEn;

    @JsonProperty("ext_id")
    private String ext_id;

    
}

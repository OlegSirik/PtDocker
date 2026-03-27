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
public class Identifier {
    @JsonProperty("isPrimary")
    private boolean isPrimary;

    @JsonProperty("typeCode")
    private String typeCode;

    @JsonProperty("serial")
    private String serial;

    @JsonProperty("number")
    private String number;

    @JsonProperty("dateIssue")
    private String dateIssue;

    @JsonProperty("validUntil")
    private String validUntil;

    @JsonProperty("whom")
    private String whom;

    @JsonProperty("divisionCode")
    private String divisionCode;

    @JsonProperty("ext_id")
    private String ext_id;

    @JsonProperty("countryCode")
    private String countryCode;
    
}

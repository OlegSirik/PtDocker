package ru.pt.api.dto.process;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Insurer {
    
    @JsonProperty("organization")
    private Organization organization;

    @JsonProperty("addresses")
    private List<Address> addresses;

    @JsonProperty("identifiers")
    private List<Identifier> identifiers;

    @JsonProperty("contactInfo")
    private ContactInfo contactInfo;
}

package ru.pt.api.dto.process;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyHolder {

    @JsonProperty("person")
    private Person person;

    @JsonProperty("organization")
    private Organization organization;

    private String phType;
    // Additional attributes and objects (flexible structure)
    private Map<String, Object> additionalAttributes = new HashMap<>();

    // Constructors
    public PolicyHolder() {
        this.additionalAttributes = new HashMap<>();
    }

    public PolicyHolder(Person person, Organization organization) {
        this();
        this.person = person;
        this.organization = organization;
    }

    // Getters and Setters
    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @JsonIgnore
    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    // Capture unknown properties during deserialization (handles both strings and nested objects)
    @JsonAnySetter
    public void setAdditionalAttribute(String key, Object value) {
        if (additionalAttributes == null) {
            additionalAttributes = new HashMap<>();
        }
        additionalAttributes.put(key, value);
    }

    // Serialize additional attributes back to JSON as top-level properties
    @JsonAnyGetter
    public Map<String, Object> getAdditionalAttributesForJson() {
        if (additionalAttributes == null) {
            additionalAttributes = new HashMap<>();
        }
        return additionalAttributes;
    }

    public String getPhType() {
        return phType;
    }

    public void setPhType(String phType) {
        this.phType = phType;
    }
}

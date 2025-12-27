package ru.pt.api.dto.process;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InsuredObject {

    @JsonProperty("packageCode")
    private Integer packageCode;  // Тип число ???????

    @JsonProperty("covers")
    private List<Cover> covers;

    @JsonProperty("objectId")
    private String objectId;

    @JsonProperty("sumInsured")
    private Double sumInsured;

    // Additional attributes
    private Map<String, Object> additionalAttributes;

    private String ioType;
    // Constructors
    public InsuredObject() {}

    public InsuredObject(Double sumInsured, Integer packageCode, List<Cover> covers, String objectId) {
        
        this.sumInsured = sumInsured;
        this.packageCode = packageCode;
        this.covers = covers;
        this.objectId = objectId;
    }

    // Getters and Setters
    public Double getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(Double sumInsured) {
        this.sumInsured = sumInsured;
    }

    public Integer getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(Integer packageCode) {
        this.packageCode = packageCode;
    }

    public List<Cover> getCovers() {
        if (covers == null) {
            covers = new ArrayList<Cover>();
        }
        return covers;
    }

    public void setCovers(List<Cover> covers) {
        this.covers = covers;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @JsonIgnore
    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    // Capture unknown properties during deserialization
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
        return additionalAttributes != null && !additionalAttributes.isEmpty() ? additionalAttributes : null;
    }

    public String getIoType() {
        return ioType;
    }

    public void setIoType(String ioType) {
        this.ioType = ioType;
    }
}

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

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyHolder {


    // Additional attributes
    private Map<String, Object> additionalAttributes;

    // Constructors
    public PolicyHolder() {}


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
    
}

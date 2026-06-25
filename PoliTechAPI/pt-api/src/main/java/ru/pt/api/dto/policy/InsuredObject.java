package ru.pt.api.dto.policy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsuredObject {

    private String packageCode;
    private List<Cover> covers;
    private String objectId;
    private BigDecimal sumInsured;
    private Map<String, Object> additionalAttributes;
    private String ioType;

    public InsuredObject() {
    }

    public InsuredObject(BigDecimal sumInsured, String packageCode, List<Cover> covers, String objectId) {
        this.sumInsured = sumInsured;
        this.packageCode = packageCode;
        this.covers = covers;
        this.objectId = objectId;
    }

    public BigDecimal getSumInsured() {
        return sumInsured;
    }

    public void setSumInsured(BigDecimal sumInsured) {
        this.sumInsured = sumInsured;
    }

    public String getPackageCode() {
        return packageCode;
    }

    public void setPackageCode(String packageCode) {
        this.packageCode = packageCode;
    }

    public List<Cover> getCovers() {
        if (covers == null) {
            covers = new ArrayList<>();
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

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public void setAdditionalAttribute(String key, Object value) {
        if (additionalAttributes == null) {
            additionalAttributes = new HashMap<>();
        }
        additionalAttributes.put(key, value);
    }

    public String getIoType() {
        return ioType;
    }

    public void setIoType(String ioType) {
        this.ioType = ioType;
    }
}

package ru.pt.api.dto.process;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import ru.pt.api.dto.commission.CommissionDto;
import ru.pt.api.dto.addon.PolicyAddOnDto;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PolicyDTO {

    @JsonProperty("publicId")
    private String publicId;

    @JsonProperty("draftId")
    private String draftId;

    @JsonProperty("productCode")
    private String productCode;

    @JsonProperty("waitingPeriod")
    private String waitingPeriod;

    @JsonProperty("policyTerm")
    private String policyTerm;

    @JsonProperty("startDate")
    private ZonedDateTime startDate;

    @JsonProperty("endDate")
    private ZonedDateTime endDate;

    @JsonProperty("issueDate")
    private ZonedDateTime issueDate;

    @JsonProperty("installmentType")
    private String installmentType;

    @JsonProperty("insuredObjects")
    private List<InsuredObject> insuredObjects;

    // Additional attributes and objects (flexible structure)
    private Map<String, Object> additionalAttributes = new HashMap<>();

    @JsonProperty("premium")
    private BigDecimal premium;

    @JsonProperty("policyNumber")
    private String policyNumber;

    @JsonProperty("productVersion")
    private Integer productVersion;

    @JsonProperty("id")
    private String id;

    @JsonProperty("statusCode")
    private String statusCode;

    private ProcessList processList;

    private CommissionDto commission;

    private List<PolicyAddOnDto> options;
    // Constructors
    public PolicyDTO() {
        this.additionalAttributes = new HashMap<>();
    }

    public PolicyDTO(String draftId, String productCode, String waitingPeriod, String policyTerm,
                  ZonedDateTime startDate, ZonedDateTime endDate, ZonedDateTime issueDate,
                  String installmentType,
                  List<InsuredObject> insuredObjects
) {
        this();
        this.draftId = draftId;
        this.productCode = productCode;
        this.waitingPeriod = waitingPeriod;
        this.policyTerm = policyTerm;
        this.startDate = startDate;
        this.endDate = endDate;
        this.issueDate = issueDate;
        this.installmentType = installmentType;
        this.insuredObjects = insuredObjects;
    }

    // Getters and Setters
    public String getDraftId() {
        return draftId;
    }

    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getWaitingPeriod() {
        return waitingPeriod;
    }

    public void setWaitingPeriod(String waitingPeriod) {
        this.waitingPeriod = waitingPeriod;
    }

    public String getPolicyTerm() {
        return policyTerm;
    }

    public void setPolicyTerm(String policyTerm) {
        this.policyTerm = policyTerm;
    }

    public ZonedDateTime getStartDate() {
            return startDate;
        }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public ZonedDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(ZonedDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public String getInstallmentType() {
        return installmentType;
    }

    public void setInstallmentType(String installmentType) {
        this.installmentType = installmentType;
    }


    public List<InsuredObject> getInsuredObjects() {
        return insuredObjects;
    }

    public void setInsuredObjects(List<InsuredObject> insuredObjects) {
        this.insuredObjects = insuredObjects;
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

    public BigDecimal getPremium() {
        return premium;
    }

    public void setPremium(BigDecimal premium) {
        this.premium = premium;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Integer getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(Integer productVersion) {
        this.productVersion = productVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public ProcessList getProcessList() {
        return processList;
    }

    public void setProcessList(ProcessList processList) {
        this.processList = processList;
    }

    public CommissionDto getCommission() {
        return this.commission;

    }

    public void setCommission(CommissionDto commission) {
        this.commission = commission;
        
    }

    public List<PolicyAddOnDto> getOptions() {
        return this.options;
    }

    public void setOptions(List<PolicyAddOnDto> options) {
        this.options = options;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getPublicId() {
        return this.publicId;
    }

}

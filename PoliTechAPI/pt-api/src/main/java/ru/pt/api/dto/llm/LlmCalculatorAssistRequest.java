package ru.pt.api.dto.llm;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.pt.api.dto.calculator.CalculatorModel;

public class LlmCalculatorAssistRequest {

    private LlmTaskType taskType;
    private String userMessage;
    private Long productId;
    private Long versionNo;
    private String packageNo;
    @JsonProperty("currentCalculator")
    @JsonAlias("calculator")
    private CalculatorModel currentCalculator;
    private String providerCode;
    private String model;

    public LlmTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(LlmTaskType taskType) {
        this.taskType = taskType;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Long versionNo) {
        this.versionNo = versionNo;
    }

    public String getPackageNo() {
        return packageNo;
    }

    public void setPackageNo(String packageNo) {
        this.packageNo = packageNo;
    }

    public CalculatorModel getCurrentCalculator() {
        return currentCalculator;
    }

    public void setCurrentCalculator(CalculatorModel currentCalculator) {
        this.currentCalculator = currentCalculator;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}

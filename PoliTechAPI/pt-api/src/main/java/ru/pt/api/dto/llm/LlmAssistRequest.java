package ru.pt.api.dto.llm;

public class LlmAssistRequest {

    private LlmTaskType taskType;
    private String userMessage;
    private Long productId;
    private Long versionNo;
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

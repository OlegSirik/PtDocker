package ru.pt.api.dto.llm;

public class LlmSchemaAssistRequest {

    private LlmTaskType taskType = LlmTaskType.SCHEMA;
    private String userMessage;
    private String contractCode;
    /** Optional hint: attach new nodes under this existing varCode. */
    private String parentVarCode;
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

    public String getContractCode() {
        return contractCode;
    }

    public void setContractCode(String contractCode) {
        this.contractCode = contractCode;
    }

    public String getParentVarCode() {
        return parentVarCode;
    }

    public void setParentVarCode(String parentVarCode) {
        this.parentVarCode = parentVarCode;
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

package ru.pt.api.dto.llm;

public class LlmLobAssistRequest {

    private LlmTaskType taskType;
    private String userMessage;
    private String lobCode;
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

    public String getLobCode() {
        return lobCode;
    }

    public void setLobCode(String lobCode) {
        this.lobCode = lobCode;
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

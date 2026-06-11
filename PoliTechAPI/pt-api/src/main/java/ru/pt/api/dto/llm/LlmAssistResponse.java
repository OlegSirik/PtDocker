package ru.pt.api.dto.llm;

import java.util.ArrayList;
import java.util.List;

public class LlmAssistResponse {

    private boolean success;
    private LlmTaskType taskType;
    private Object result;
    private String rawContent;
    private List<String> warnings = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private LlmUsage usage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public LlmTaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(LlmTaskType taskType) {
        this.taskType = taskType;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public LlmUsage getUsage() {
        return usage;
    }

    public void setUsage(LlmUsage usage) {
        this.usage = usage;
    }
}

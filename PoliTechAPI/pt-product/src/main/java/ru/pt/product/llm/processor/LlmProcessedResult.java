package ru.pt.product.llm.processor;

import java.util.ArrayList;
import java.util.List;

public class LlmProcessedResult {

    private final boolean success;
    private final Object result;
    private final List<String> warnings;
    private final List<String> errors;

    private LlmProcessedResult(boolean success, Object result, List<String> warnings, List<String> errors) {
        this.success = success;
        this.result = result;
        this.warnings = warnings;
        this.errors = errors;
    }

    public static LlmProcessedResult ok(Object result, List<String> warnings) {
        return new LlmProcessedResult(true, result, warnings != null ? warnings : List.of(), List.of());
    }

    public static LlmProcessedResult fail(List<String> errors) {
        return new LlmProcessedResult(false, null, List.of(), errors != null ? errors : List.of());
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getResult() {
        return result;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> mutableWarnings() {
        return new ArrayList<>(warnings);
    }
}

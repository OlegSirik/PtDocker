package ru.pt.api.dto.rules;

public class RuleDto {
    private Long id;
    private String code;
    private String name;
    private RuleScopeType scopeType;
    private String scopeCode;
    private RuleType ruleType;
    private Integer priority;
    private String recordStatus;
    private String expressionLanguage;
    private String expression;
    private String message;
    private String llmText;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(RuleScopeType scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeCode() {
        return scopeCode;
    }

    public void setScopeCode(String scopeCode) {
        this.scopeCode = scopeCode;
    }

    public RuleType getRuleType() {
        return ruleType;
    }

    public void setRuleType(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }

    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    public void setExpressionLanguage(String expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLlmText() {
        return llmText;
    }

    public void setLlmText(String llmText) {
        this.llmText = llmText;
    }
}

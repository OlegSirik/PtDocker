package ru.pt.api.dto.rules;

import java.util.Map;

public class RuleValidationContext {
    private Long tid;
    private String tenantCode;
    private String productCode;
    private String lobCode;
    private String clientId;
    private Map<String, Object> variables;

    public RuleValidationContext() {
    }

    public RuleValidationContext(
            Long tid,
            String tenantCode,
            String productCode,
            String lobCode,
            String clientId,
            Map<String, Object> variables) {
        this.tid = tid;
        this.tenantCode = tenantCode;
        this.productCode = productCode;
        this.lobCode = lobCode;
        this.clientId = clientId;
        this.variables = variables;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getLobCode() {
        return lobCode;
    }

    public void setLobCode(String lobCode) {
        this.lobCode = lobCode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
}

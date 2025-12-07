package ru.pt.auth.model;

public class AdminResponse {
    private Long id;
    private Long tid;
    private String tenantCode;
    private Long clientId;
    private Long accountId;
    private String userLogin;
    private String userRole;
    private String fullName;
    private String position;


    public AdminResponse() {
    }

    public AdminResponse(Long id, Long tid, String tenantCode, Long clientId, Long accountId, String userLogin, String userRole,
                         String fullName, String position) {
        this.id = id;
        this.tid = tid;
        this.tenantCode = tenantCode;
        this.clientId = clientId;
        this.accountId = accountId;
        this.userLogin = userLogin;
        this.userRole = userRole;
        this.fullName = fullName;
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

}

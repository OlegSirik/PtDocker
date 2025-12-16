package ru.pt.api.dto.auth;

/**
 * Конфигурация под инстанс приложения для потребителя платформы
 */
public class ClientConfiguration {
    // тип платежного шлюза
    private String paymentGate;
    // отправлять ли email после оплаты
    private boolean sendEmailAfterBuy;
    // отправлять ли смс после оплаты
    private boolean sendSmsAfterBuy;
    // номер договора для платежного шлюза
    private String paymentGateAgentNumber;
    // логин платежного шлюза
    private String paymentGateLogin;
    // пароль для платежного шлюза
    private String paymentGatePassword;
    // email сотрудника, которому отправлять email вместо владельца полиса
    private String employeeEmail;
    // тип e-mail шлюза
    private String emailGate;
    // логин e-mail шлюза
    private String emailLogin;
    // пароль e-mail шлюза
    private String emailPassword;


    public ClientConfiguration() {
    }

    public ClientConfiguration(String paymentGate,
                               boolean sendEmailAfterBuy,
                               boolean sendSmsAfterBuy,
                               String paymentGateAgentNumber,
                               String paymentGateLogin,
                               String paymentGatePassword,
                               String employeeEmail,
                               String emailGate,
                               String emailLogin,
                               String emailPassword) {
        this.paymentGate = paymentGate;
        this.sendEmailAfterBuy = sendEmailAfterBuy;
        this.sendSmsAfterBuy = sendSmsAfterBuy;
        this.paymentGateAgentNumber = paymentGateAgentNumber;
        this.paymentGateLogin = paymentGateLogin;
        this.paymentGatePassword = paymentGatePassword;
        this.employeeEmail = employeeEmail;
        this.emailGate = emailGate;
        this.emailLogin = emailLogin;
        this.emailPassword = emailPassword;
    }

    public String getPaymentGate() {
        return paymentGate;
    }

    public void setPaymentGate(String paymentGate) {
        this.paymentGate = paymentGate;
    }

    public boolean isSendEmailAfterBuy() {
        return sendEmailAfterBuy;
    }

    public void setSendEmailAfterBuy(boolean sendEmailAfterBuy) {
        this.sendEmailAfterBuy = sendEmailAfterBuy;
    }

    public boolean isSendSmsAfterBuy() {
        return sendSmsAfterBuy;
    }

    public void setSendSmsAfterBuy(boolean sendSmsAfterBuy) {
        this.sendSmsAfterBuy = sendSmsAfterBuy;
    }

    public String getPaymentGateAgentNumber() {
        return paymentGateAgentNumber;
    }

    public void setPaymentGateAgentNumber(String paymentGateAgentNumber) {
        this.paymentGateAgentNumber = paymentGateAgentNumber;
    }

    public String getPaymentGateLogin() {
        return paymentGateLogin;
    }

    public void setPaymentGateLogin(String paymentGateLogin) {
        this.paymentGateLogin = paymentGateLogin;
    }

    public String getPaymentGatePassword() {
        return paymentGatePassword;
    }

    public void setPaymentGatePassword(String paymentGatePassword) {
        this.paymentGatePassword = paymentGatePassword;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmailGate() {
        return emailGate;
    }

    public void setEmailGate(String emailGate) {
        this.emailGate = emailGate;
    }

    public String getEmailLogin() {
        return emailLogin;
    }

    public void setEmailLogin(String emailLogin) {
        this.emailLogin = emailLogin;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }
}

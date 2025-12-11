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


    public ClientConfiguration() {
    }

    public ClientConfiguration(String paymentGate, boolean sendEmailAfterBuy, boolean sendSmsAfterBuy, String paymentGateAgentNumber, String paymentGateLogin, String paymentGatePassword, String employeeEmail) {
        this.paymentGate = paymentGate;
        this.sendEmailAfterBuy = sendEmailAfterBuy;
        this.sendSmsAfterBuy = sendSmsAfterBuy;
        this.paymentGateAgentNumber = paymentGateAgentNumber;
        this.paymentGateLogin = paymentGateLogin;
        this.paymentGatePassword = paymentGatePassword;
        this.employeeEmail = employeeEmail;
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
}

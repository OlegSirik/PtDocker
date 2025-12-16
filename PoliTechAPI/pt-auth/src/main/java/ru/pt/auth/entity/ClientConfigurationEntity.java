package ru.pt.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "acc_client_configuration")
public class ClientConfigurationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "acc_client_configuration_seq")
    @SequenceGenerator(name = "acc_client_configuration_seq", sequenceName = "acc_client_configuration_seq", allocationSize = 1)
    private Long id;

    // тип платежного шлюза
    @Column(name = "payment_gate")
    private String paymentGate;
    // отправлять ли email после оплаты
    @Column(name = "send_email_after_buy")
    private boolean sendEmailAfterBuy;
    // отправлять ли смс после оплаты
    @Column(name = "send_sms_after_buy")
    private boolean sendSmsAfterBuy;
    // номер договора для платежного шлюза
    @Column(name = "pg_agent_number")
    private String paymentGateAgentNumber;
    // логин платежного шлюза
    @Column(name = "pg_client_login")
    private String paymentGateLogin;
    // пароль для платежного шлюза
    @Column(name = "pg_client_password")
    private String paymentGatePassword;
    // email сотрудника, которому отправлять email вместо владельца полиса
    @Column(name = "client_employee_email")
    private String employeeEmail;
    // тип e-mail шлюза
    @Column(name = "email_gate")
    private String emailGate;
    // логин e-mail шлюза
    @Column(name = "email_login")
    private String emailLogin;
    // пароль e-mail шлюза
    @Column(name = "email_password")
    private String emailPassword;

    public ClientConfigurationEntity() {
    }

    public ClientConfigurationEntity(Long id,
                                     String paymentGate,
                                     boolean sendEmailAfterBuy,
                                     boolean sendSmsAfterBuy,
                                     String paymentGateAgentNumber,
                                     String paymentGateLogin,
                                     String paymentGatePassword,
                                     String employeeEmail,
                                     String emailGate,
                                     String emailLogin,
                                     String emailPassword) {
        this.id = id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

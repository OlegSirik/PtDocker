package ru.pt.payments.model.vsk;

public class PrimaryPersonDataModel {

    private String firstName;
    private String middleName;
    private Boolean noMiddleNameFlag;
    private String lastName;
    private DateModel birthDate;

    public PrimaryPersonDataModel() {
    }

    public PrimaryPersonDataModel(String firstName, String middleName, Boolean noMiddleNameFlag, String lastName, DateModel birthDate) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.noMiddleNameFlag = noMiddleNameFlag;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Boolean getNoMiddleNameFlag() {
        return noMiddleNameFlag;
    }

    public void setNoMiddleNameFlag(Boolean noMiddleNameFlag) {
        this.noMiddleNameFlag = noMiddleNameFlag;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public DateModel getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(DateModel birthDate) {
        this.birthDate = birthDate;
    }

}

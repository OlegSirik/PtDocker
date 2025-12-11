package ru.pt.payments.model.vsk;

public class UnidentifiedPayerModel {

    private String systemId;
    private PersonDataModel personDataModel;

    public UnidentifiedPayerModel(String systemId, PersonDataModel personDataModel) {
        this.systemId = systemId;
        this.personDataModel = personDataModel;
    }

    public UnidentifiedPayerModel() {
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public PersonDataModel getPersonDataModel() {
        return personDataModel;
    }

    public void setPersonDataModel(PersonDataModel personDataModel) {
        this.personDataModel = personDataModel;
    }


}

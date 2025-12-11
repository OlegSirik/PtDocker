package ru.pt.payments.model.vsk;

public class PersonDataModel {

    private PrimaryPersonDataModel primaryPersonDataModel;
    private String birthPlace;
    private String citizenship;
    private Long maritalStatusId;
    private Long genderId;
    private String snilsNumber;
    private String innNumber;

    public PersonDataModel() {
    }

    public PersonDataModel(PrimaryPersonDataModel primaryPersonDataModel, String birthPlace, String citizenship, Long maritalStatusId, Long genderId, String snilsNumber, String innNumber) {
        this.primaryPersonDataModel = primaryPersonDataModel;
        this.birthPlace = birthPlace;
        this.citizenship = citizenship;
        this.maritalStatusId = maritalStatusId;
        this.genderId = genderId;
        this.snilsNumber = snilsNumber;
        this.innNumber = innNumber;
    }

    public PrimaryPersonDataModel getPrimaryPersonDataModel() {
        return primaryPersonDataModel;
    }

    public void setPrimaryPersonDataModel(PrimaryPersonDataModel primaryPersonDataModel) {
        this.primaryPersonDataModel = primaryPersonDataModel;
    }

    public String getBirthPlace() {
        return birthPlace;
    }

    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public Long getMaritalStatusId() {
        return maritalStatusId;
    }

    public void setMaritalStatusId(Long maritalStatusId) {
        this.maritalStatusId = maritalStatusId;
    }

    public Long getGenderId() {
        return genderId;
    }

    public void setGenderId(Long genderId) {
        this.genderId = genderId;
    }

    public String getSnilsNumber() {
        return snilsNumber;
    }

    public void setSnilsNumber(String snilsNumber) {
        this.snilsNumber = snilsNumber;
    }

    public String getInnNumber() {
        return innNumber;
    }

    public void setInnNumber(String innNumber) {
        this.innNumber = innNumber;
    }

}

package ru.pt.payments.model.vsk;

public class IdentifiedPayerModel {

    private String systemId;
    private PrimaryPersonDataModel primaryPersonDataModel;
    private PrimaryDocumentInfoModel primaryDocumentInfoModel;

    public IdentifiedPayerModel() {
    }

    public IdentifiedPayerModel(String systemId,
                                PrimaryPersonDataModel primaryPersonDataModel,
                                PrimaryDocumentInfoModel primaryDocumentInfoModel) {
        this.systemId = systemId;
        this.primaryPersonDataModel = primaryPersonDataModel;
        this.primaryDocumentInfoModel = primaryDocumentInfoModel;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public PrimaryPersonDataModel getPrimaryPersonDataModel() {
        return primaryPersonDataModel;
    }

    public void setPrimaryPersonDataModel(PrimaryPersonDataModel primaryPersonDataModel) {
        this.primaryPersonDataModel = primaryPersonDataModel;
    }

    public PrimaryDocumentInfoModel getPrimaryDocumentInfoModel() {
        return primaryDocumentInfoModel;
    }

    public void setPrimaryDocumentInfoModel(PrimaryDocumentInfoModel primaryDocumentInfoModel) {
        this.primaryDocumentInfoModel = primaryDocumentInfoModel;
    }
}

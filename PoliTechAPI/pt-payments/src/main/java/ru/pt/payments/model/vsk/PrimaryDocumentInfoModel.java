package ru.pt.payments.model.vsk;

public class PrimaryDocumentInfoModel {

    private Long documentTypeId;
    private String documentSerialNumber;

    public PrimaryDocumentInfoModel() {
    }

    public PrimaryDocumentInfoModel(Long documentTypeId, String documentSerialNumber) {
        this.documentTypeId = documentTypeId;
        this.documentSerialNumber = documentSerialNumber;
    }

    public Long getDocumentTypeId() {
        return documentTypeId;
    }

    public void setDocumentTypeId(Long documentTypeId) {
        this.documentTypeId = documentTypeId;
    }

    public String getDocumentSerialNumber() {
        return documentSerialNumber;
    }

    public void setDocumentSerialNumber(String documentSerialNumber) {
        this.documentSerialNumber = documentSerialNumber;
    }

}

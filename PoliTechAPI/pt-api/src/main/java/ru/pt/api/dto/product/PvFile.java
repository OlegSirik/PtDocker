package ru.pt.api.dto.product;

public class PvFile {
    @com.fasterxml.jackson.annotation.JsonProperty("fileCode")
    private String fileCode;

    @com.fasterxml.jackson.annotation.JsonProperty("fileName")
    private String fileName;

    @com.fasterxml.jackson.annotation.JsonProperty("fileId")
    private Integer fileId;

    public PvFile() {
    }

    public PvFile(String fileCode, String fileName, Integer fileId) {
        this.fileCode = fileCode;
        this.fileName = fileName;
        this.fileId = fileId;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }
}

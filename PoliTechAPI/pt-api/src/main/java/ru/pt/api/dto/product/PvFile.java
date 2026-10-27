package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PvFile {
    @JsonProperty("fileCode")
    private String fileCode;

    @JsonProperty("fileName")
    private String fileName;

    @JsonProperty("fileId")
    private Integer fileId;

    /** Порядковый номер части ПФ (для многостраничного merge). Legacy без поля → 1. */
    @JsonProperty("fileNr")
    private Integer fileNr;

    /** true — PDF берётся как есть, без AcroForm. Legacy без поля → false. */
    @JsonProperty("isStatic")
    private Boolean isStatic;

    public PvFile() {
    }

    public PvFile(String fileCode, String fileName, Integer fileId) {
        this.fileCode = fileCode;
        this.fileName = fileName;
        this.fileId = fileId;
        this.fileNr = 1;
        this.isStatic = false;
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

    public Integer getFileNr() {
        return fileNr;
    }

    public void setFileNr(Integer fileNr) {
        this.fileNr = fileNr;
    }

    /** Эффективный номер части: null → 1. */
    public int effectiveFileNr() {
        return fileNr != null && fileNr > 0 ? fileNr : 1;
    }

    public Boolean getIsStatic() {
        return isStatic;
    }

    public void setIsStatic(Boolean isStatic) {
        this.isStatic = isStatic;
    }

    public boolean isStaticFile() {
        return Boolean.TRUE.equals(isStatic);
    }
}

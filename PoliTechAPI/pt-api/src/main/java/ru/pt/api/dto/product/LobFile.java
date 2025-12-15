package ru.pt.api.dto.product;

import com.fasterxml.jackson.annotation.*;

public class LobFile {
    @JsonProperty("fileCode")
    private String fileCode;

    @JsonProperty("fileName")
    private String filename;

    public LobFile() {}

    public LobFile(String fileCode, String filename) {
        this.fileCode = fileCode;
        this.filename = filename;
    }

    public String getFileCode() {
        return fileCode;
    }

    public void setFileCode(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

package ru.pt.api.dto.file;

public enum FileStorageType {
    DB("DB"),
    FS("FS"),
    MINIO("MINIO"),
    S3("S3");

    private final String value;

    FileStorageType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}

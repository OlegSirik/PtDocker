package ru.pt.api.dto.file;

public enum StorageProperty {

    MAX_SIZE("maxSize"), // Максимальный размер файла в байтах

    // MinIO
    END_POINT("endPoint"), // URL для MinIO
    ACCESS_KEY("accessKey"), // Ключ доступа для MinIO
    SECRET_KEY("secretKey"), // Секретный ключ для MinIO
    BUCKET("bucket"), // Имя бакета для MinIO

    // FileSystem
    BASE_DIR("baseDir"),  // Базовая директория для хранения файлов

    // S3
    REGION("region"), // Регион для S3
    PREFIX("prefix"); // Префикс для ключа файла

    private final String value;

    StorageProperty(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}

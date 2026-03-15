package ru.pt.files.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import ru.pt.api.service.file.FileStorage;
import ru.pt.files.service.FileSystemStorage;
import ru.pt.files.service.S3Storage;
import ru.pt.files.service.DatabaseStorage;

@Configuration
public class StorageConfig {

    @Bean
    public FileStorage fileStorage(
            @Value("${storage.type:db}") String type,
            FileSystemStorage fs,
            S3Storage s3,
            DatabaseStorage db) {

        return switch (type) {
            case "fs" -> fs;
            case "s3" -> s3;
            case "db" -> db;
            default -> db;
        };
    }
}

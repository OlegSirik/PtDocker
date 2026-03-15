package ru.pt.api.service.file;

import java.io.InputStream;
import java.util.Map;

import ru.pt.api.dto.file.FileStorageType;

public interface FileStorage {

    String store(Long tid, String key, Map<String, String> config,InputStream stream);
    InputStream load(String key, Map<String, String> config);
    void delete(String key, Map<String, String> config);
    boolean exists(String key, Map<String, String> config);
    boolean supports(FileStorageType type);
} 

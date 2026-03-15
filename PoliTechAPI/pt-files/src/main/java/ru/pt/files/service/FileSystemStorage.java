package ru.pt.files.service;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.dto.file.StorageProperty;
import ru.pt.api.service.file.FileStorage;

@Component
public class FileSystemStorage implements FileStorage {

    private Path getRoot(Map<String, String> config, String key) {
        Path root = Paths.get(config.get(StorageProperty.BASE_DIR.getValue()));
        if (root == null) {
            throw new RuntimeException("Base directory not found in config");
        }
        if (!Files.exists(root)) {
            throw new RuntimeException("Base directory not found: " + root.toString());
        }
        return root.resolve(key);
    }

    @Override
    public String store(Long tid, String key, Map<String, String> config, InputStream stream) {
        
        Path path = getRoot(config, key);

        try {
            Files.copy(stream, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return key;
    }

    @Override
    public InputStream load(String key, Map<String, String> config) {

        try {
            return Files.newInputStream(getRoot(config, key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String key, Map<String, String> config) {

        try {
            Files.deleteIfExists(getRoot(config, key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String key, Map<String, String> config) {
        return Files.exists(getRoot(config, key));
    }

    @Override
    public boolean supports(FileStorageType type) {
        return type == FileStorageType.FS;
    }
}

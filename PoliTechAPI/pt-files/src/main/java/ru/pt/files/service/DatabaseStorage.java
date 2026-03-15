package ru.pt.files.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.service.file.FileStorage;
import ru.pt.auth.security.SecurityContextHelper;
import ru.pt.files.entity.FileEntity;
import ru.pt.files.repository.FileRepository;

@Component
@RequiredArgsConstructor
public class DatabaseStorage implements FileStorage {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseStorage.class);

    private final FileRepository fileRepository;
    private final SecurityContextHelper securityContextHelper;

    @Override
    public String store(Long tid, String key, Map<String, String> config, InputStream stream) {

        byte[] data;
        try {
            data = stream.readAllBytes();
        } catch (IOException ex) {
            logger.error("Failed to read stream for file: {}", key, ex);
            throw new RuntimeException("Failed to read file content", ex);
        }

        FileEntity entity = fileRepository.findByPublicId(key).orElse(null);

        if (entity != null) {
            entity.setFileBody(data);
            entity.setSize((long) data.length);
            fileRepository.save(entity);
        } else {
            entity = new FileEntity();
            entity.setPublicId(key);
            entity.setFileBody(data);
            entity.setSize((long) data.length);
            entity.setTid(tid);
            fileRepository.save(entity);
        }

        return key;
    }

    @Override
    public InputStream load(String key, Map<String, String> config) {
        FileEntity entity = fileRepository.findByPublicId(key)
                .orElseThrow(() -> new RuntimeException("File not found: " + key));
        byte[] body = entity.getFileBody();
        if (body == null) {
            throw new RuntimeException("File body is empty: " + key);
        }
        return new ByteArrayInputStream(body);
    }

    @Override
    public void delete(String key, Map<String, String> config) {
        fileRepository.findByPublicId(key).ifPresent(fileRepository::delete);
    }

    @Override
    public boolean exists(String key, Map<String, String> config) {
        return fileRepository.findByPublicId(key).isPresent();
    }

    @Override
    public boolean supports(FileStorageType type) {
        return type == FileStorageType.DB;
    }
}

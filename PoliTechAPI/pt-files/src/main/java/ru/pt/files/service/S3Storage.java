package ru.pt.files.service;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.service.file.FileStorage;

@Component
public class S3Storage implements FileStorage {

    //private final S3Client client;
    //private final String bucket;

    @Override
    public String store(Long tid, String key, Map<String, String> config, InputStream stream) {

        

        /* 
        client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build(),
            RequestBody.fromInputStream(stream, -1)
        );
        */
        return key;
    }

    @Override
    public InputStream load(String key, Map<String, String> config) {

        /* ResponseInputStream<GetObjectResponse> object =
            client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            );

        return object;
        */
        return null;
    }

    @Override
    public void delete(String key, Map<String, String> config) {

        /* client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
        );
        */
    }

    @Override
    public boolean exists(String key, Map<String, String> config) {

        /* try {
            client.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
        */
        return false;
    }

    @Override
    public boolean supports(FileStorageType type) {
        return type == FileStorageType.S3;
    }
}

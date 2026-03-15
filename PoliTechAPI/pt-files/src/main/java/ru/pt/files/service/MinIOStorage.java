package ru.pt.files.service;

import java.io.InputStream;
import java.util.Map;

import org.springframework.stereotype.Component;

import ru.pt.api.dto.file.FileStorageType;
import ru.pt.api.dto.file.StorageProperty;
import ru.pt.api.service.file.FileStorage;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MinIOStorage implements FileStorage {

    private final Map<ClientKey, MinioClient> minioClients = new ConcurrentHashMap<>();

    //private final String endPoint;
    //private final String accessKey;
    //private final String secretKey;
    //private final String bucket;

    //private final MinioClient minioClient;
    //private final MinioProperties props;

    @Override
    public String store(Long tid, String key, Map<String, String> config, InputStream stream) {

        String endPoint = config.get(StorageProperty.END_POINT.getValue()); // http://localhost:9000
        String accessKey = config.get(StorageProperty.ACCESS_KEY.getValue()); // minioadmin
        String secretKey = config.get(StorageProperty.SECRET_KEY.getValue()); // minioadmin
        String bucket = config.get(StorageProperty.BUCKET.getValue());

        
        MinioClient client = getClient(config);
        try {
            client.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        // unknown size: -1, with a reasonable part size (10 MiB)
                        .stream(stream, -1, 10 * 1024 * 1024)
                        .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return key;
    }

    @Override
    public InputStream load(String key, Map<String, String> config) {

        String endPoint = config.get(StorageProperty.END_POINT.getValue()); // http://localhost:9000
        String accessKey = config.get(StorageProperty.ACCESS_KEY.getValue()); // minioadmin
        String secretKey = config.get(StorageProperty.SECRET_KEY.getValue()); // minioadmin
        String bucket = config.get(StorageProperty.BUCKET.getValue());

        MinioClient client = getClient(config);

        try {
            return client.getObject(
            GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String key, Map<String, String> config) {

        String endPoint = config.get(StorageProperty.END_POINT.getValue()); // http://localhost:9000
        String accessKey = config.get(StorageProperty.ACCESS_KEY.getValue()); // minioadmin
        String secretKey = config.get(StorageProperty.SECRET_KEY.getValue()); // minioadmin
        String bucket = config.get(StorageProperty.BUCKET.getValue());

        MinioClient client = getClient(config);
        try {
            client.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(key)
                        .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String key, Map<String, String> config) {

        String endPoint = config.get(StorageProperty.END_POINT.getValue()); // http://localhost:9000
        String accessKey = config.get(StorageProperty.ACCESS_KEY.getValue()); // minioadmin
        String secretKey = config.get(StorageProperty.SECRET_KEY.getValue()); // minioadmin
        String bucket = config.get(StorageProperty.BUCKET.getValue());

        try {
            getClient(config).statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .build()
            );
            return true;
        } catch (Exception e) {
            // If stat fails (e.g. object not found), treat as non-existent
            return false;
        }
    }

    @Override
    public boolean supports(FileStorageType type) {
        return type == FileStorageType.MINIO;
    }

    private MinioClient getClient(Map<String, String> config) {

        ClientKey key = new ClientKey(
                config.get(StorageProperty.END_POINT.getValue()),
                config.get(StorageProperty.ACCESS_KEY.getValue()),
                config.get(StorageProperty.SECRET_KEY.getValue())
        );

        return minioClients.computeIfAbsent(key, this::createClient);
    }

    private MinioClient createClient(ClientKey key) {

        return MinioClient.builder()
                .endpoint(key.endpoint)
                .credentials(key.accessKey, key.secretKey)
                .build();
    }

    private record ClientKey(
            String endpoint,
            String accessKey,
            String secretKey
    ) {}    
}

package com.future_sign.document_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.minio.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;


@Service
public class DocumentStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public DocumentStorageService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Created bucket: " + bucketName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket", e);
        }
    }

    public String uploadPdf(byte[] content) {
        try {
            String fileKey = UUID.randomUUID().toString();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .contentType("application/pdf")
                            .build());
            return fileKey;
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO", e);
        }
    }

    public byte[] downloadPdf(String fileKey) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileKey)
                            .build());
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from MinIO", e);
        }
    }
}

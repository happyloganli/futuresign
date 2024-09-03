package com.future_sign.document_service;

import io.minio.http.Method;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.minio.*;
import java.io.ByteArrayInputStream;

import java.util.UUID;


@Service
public class DocumentService {

    private final MinioClient minioClient;
    private final String bucketName;

    public DocumentService(
            @Value("${minio.endpoint}") String endpoint,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey,
            @Value("${minio.bucket-name}") String bucketName,
            RabbitTemplate rabbitTemplate) {
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

    public String downloadPdf(String fileKey) {
        try {
            // Generate a pre-signed URL that is valid for 1 hour (3600 seconds)
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileKey)
                            .expiry(3600) // URL expiration time in seconds
                            .build());

            return url;
        } catch (Exception e) {
            throw new RuntimeException("Error generating download link for file from MinIO", e);
        }
    }
}

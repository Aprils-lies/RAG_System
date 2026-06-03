package com.april.rag.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class MinioConfig {

    private static final Logger logger = LoggerFactory.getLogger(MinioConfig.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.publicUrl}")
    private String publicUrl;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public String minioPublicUrl() {
        return publicUrl;
    }
}

// 单独的组件来初始化 buckets
@Component
class MinioBucketInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MinioBucketInitializer.class);

    @Autowired
    private MinioClient minioClient;

    private static final String[] BUCKETS = {"uploads", "documents", "avatars"};

    @Override
    public void run(ApplicationArguments args) {
        try {
            for (String bucketName : BUCKETS) {
                createBucketIfNotExists(bucketName);
            }
            logger.info("MinIO buckets initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize MinIO buckets: {}", e.getMessage());
        }
    }

    private void createBucketIfNotExists(String bucketName) {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                logger.info("✓ Bucket '{}' created successfully", bucketName);
            } else {
                logger.info("✓ Bucket '{}' already exists", bucketName);
            }
        } catch (Exception e) {
            logger.error("✗ Failed to create bucket '{}': {}", bucketName, e.getMessage());
        }
    }
}
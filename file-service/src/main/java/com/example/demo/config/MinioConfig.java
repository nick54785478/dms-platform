package com.example.demo.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;

import java.net.URI;

@Configuration
public class MinioConfig {

    @Value("${file-service.storage.endpoint}")
    private String endpoint;

    @Value("${file-service.storage.access-key}")
    private String accessKey;

    @Value("${file-service.storage.secret-key}")
    private String secretKey;

    @Value("${file-service.storage.region}")
    private String region;

    @Value("${file-service.storage.temp-bucket}")
    private String tempBucket;

    @Value("${file-service.storage.permanent-bucket}")
    private String permanentBucket;

    /**
     * 註冊 MinioClient，用於一般的 Bucket 管理、單檔上傳/下載操作
     * 同時在啟動時自動建立所需的 Buckets
     */
    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                // 如果需要設定 Region 也可以在此設定
                .build();
                
        initBucket(client, tempBucket);
        initBucket(client, permanentBucket);
        
        return client;
    }

    private void initBucket(MinioClient client, String bucketName) {
        try {
            boolean isExist = client.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!isExist) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("Bucket created: " + bucketName);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize bucket: " + bucketName + " " + e.getMessage());
        }
    }

    /**
     * 註冊 AWS S3Client，專門用來處理分片上傳 (Multipart Upload) 相容操作
     * 因為 MinIO 本身完全支援 S3 協議
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                // 針對 MinIO (S3 相容儲存) 需要開啟 Path Style Access
                // 否則預設會被解析為 virtual hosted style (bucketName.localhost:9000) 導致連線失敗
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /**
     * 註冊 S3Presigner，用來簽發分片上傳的 Pre-signed URL
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /**
     * 設定 Temp Bucket 的自動清理策略 (Lifecycle Policy)
     * 啟動時自動套用，超過 24 小時未搬移的物件會被 MinIO 自動永久刪除
     */
    @Bean
    public org.springframework.boot.CommandLineRunner setupLifecycle(S3Client s3Client) {
        return args -> {
            try {
                software.amazon.awssdk.services.s3.model.LifecycleRule rule = software.amazon.awssdk.services.s3.model.LifecycleRule.builder()
                        .id("orphan-cleanup-rule")
                        .status(software.amazon.awssdk.services.s3.model.ExpirationStatus.ENABLED)
                        .filter(software.amazon.awssdk.services.s3.model.LifecycleRuleFilter.builder().prefix("").build())
                        .expiration(software.amazon.awssdk.services.s3.model.LifecycleExpiration.builder().days(1).build())
                        .build();

                software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration config = software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration.builder()
                        .rules(rule)
                        .build();

                s3Client.putBucketLifecycleConfiguration(
                        software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest.builder()
                                .bucket(tempBucket)
                                .lifecycleConfiguration(config)
                                .build()
                );
                System.out.println("成功設定 Temp Bucket (" + tempBucket + ") 的 1天生命週期清理規則");
            } catch (Exception e) {
                System.err.println("設定 Temp Bucket 生命週期失敗 (如果剛建立可能需稍後): " + e.getMessage());
            }
        };
    }
}

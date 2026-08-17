package com.example.demo.infrastructure.helper;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioFileHelper {

    private final MinioClient client;

    @Async("minioAsyncExecutor")
    public CompletableFuture<Void> cloneFileAsynchronously(String bucket, String sourceObject, String targetObject) {
        try {
            CopySource source = CopySource.builder().bucket(bucket).object(sourceObject).build();
            client.copyObject(CopyObjectArgs.builder()
                    .bucket(bucket)
                    .object(targetObject)
                    .source(source)
                    .build());
            log.debug("非同步複製完成: {}/{} -> {}/{}", bucket, sourceObject, bucket, targetObject);
        } catch (Exception e) {
            log.error("非同步複製失敗: {}/{}", bucket, sourceObject, e);
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("minioAsyncExecutor")
    public CompletableFuture<Void> deleteFileAsynchronously(String bucket, String objectName) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            log.debug("非同步刪除完成: {}/{}", bucket, objectName);
        } catch (Exception e) {
            log.error("非同步刪除失敗: {}/{}", bucket, objectName, e);
            throw new RuntimeException(e);
        }
        return CompletableFuture.completedFuture(null);
    }
}

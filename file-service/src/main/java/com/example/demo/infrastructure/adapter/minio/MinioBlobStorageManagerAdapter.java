package com.example.demo.infrastructure.adapter.minio;

import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.domain.file.exception.FileStorageException;
import com.example.demo.application.shared.command.CloneFilePairCommand;
import com.example.demo.application.shared.dto.FileListSearchedResult;
import com.example.demo.infrastructure.helper.MinioFileHelper;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * MinIO 與 S3 Blob Storage 管理適配器 (Infrastructure Layer - Outbound Adapter)。
 * <p>
 * 實作 {@link BlobStorageManagerPort}，負責處理底層物件儲存空間 (如 MinIO) 的具體操作，
 * 包含單檔上傳、下載、複製、刪除以及大檔案的分段上傳。
 * 依據架構規範宣告為 package-private 以隱藏基礎設施實作細節。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
class MinioBlobStorageManagerAdapter implements BlobStorageManagerPort {

    private final MinioClient client;
    private final MinioFileHelper fileHelper;

    // 分段上傳與 Pre-signed URL 使用 AWS S3 原生套件處理
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public Boolean checkBucketExists(String bucket) throws Exception {
        return client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
    }

    public void createBucket(String bucket) throws Exception {
        boolean found = checkBucketExists(bucket);
        log.debug("found: {}", found);
        if (!found) {
            log.debug("bucket: {} 不存在，建立中...", bucket);
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    @Override
    public String uploadFile(String bucket, MultipartFile file) throws Exception {
        createBucket(bucket);
        String objectName = file.getOriginalFilename();
        client.putObject(PutObjectArgs.builder()
                .bucket(bucket).object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        return objectName;
    }

    @Override
    public String uploadFile(String bucket, MultipartFile file, String filePath, String fileName) throws Exception {
        createBucket(bucket);
        String objectName = StringUtils.isNotBlank(filePath) ? (filePath.endsWith("/") ? filePath : filePath + "/") + fileName : fileName;
        client.putObject(PutObjectArgs.builder()
                .bucket(bucket).object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());
        return String.format("上傳成功，路徑為 %s/%s", bucket, objectName);
    }

    @Override
    public InputStream downloadFile(String bucket, String filePath, String fileName) throws Exception {
        String objectName = StringUtils.isNotBlank(filePath) ? (filePath.endsWith("/") ? filePath : filePath + "/") + fileName : fileName;
        return client.getObject(GetObjectArgs.builder().bucket(bucket).object(objectName).build());
    }

    @Override
    public void deleteFile(String bucket, String filePath, String fileName) throws Exception {
        String objectName = StringUtils.isNotBlank(filePath) ? (filePath.endsWith("/") ? filePath : filePath + "/") + fileName : fileName;
        client.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
    }

    @Override
    public List<String> listFiles(String bucket, String prefix) {
        List<String> fileKeys = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket).prefix(prefix).recursive(true).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    fileKeys.add(item.objectName());
                }
            }
        } catch (Exception e) {
            throw new FileStorageException("取得檔案列表發生錯誤: " + e.getMessage(), e);
        }
        return fileKeys;
    }

    @Override
    public FileListSearchedResult listPagedFiles(String bucket, String prefix, String startAfter, int maxKeys) {
        List<String> fileKeys = new ArrayList<>();
        boolean hasMore = false;
        String lastKey = null;
        try {
            ListObjectsArgs.Builder builder = ListObjectsArgs.builder()
                    .bucket(bucket).prefix(prefix).recursive(true).maxKeys(maxKeys);
            if (StringUtils.isNotBlank(startAfter)) {
                builder.startAfter(startAfter);
            }
            Iterable<Result<Item>> results = client.listObjects(builder.build());
            int count = 0;
            for (Result<Item> result : results) {
                if (count >= maxKeys) {
                    hasMore = true;
                    break;
                }
                Item item = result.get();
                if (!item.isDir()) {
                    String key = item.objectName();
                    fileKeys.add(key);
                    lastKey = key;
                    count++;
                }
            }
        } catch (Exception e) {
            throw new FileStorageException("取得分頁檔案列表發生錯誤: " + e.getMessage(), e);
        }
        return new FileListSearchedResult(fileKeys, lastKey, hasMore);
    }

    @Override
    public void cloneFile(String bucket, String sourceObject, String targetObject) {
        cloneFile(bucket, sourceObject, bucket, targetObject);
    }

    @Override
    public void cloneFile(String sourceBucket, String sourceObject, String targetBucket, String targetObject) {
        try {
            CopySource source = CopySource.builder().bucket(sourceBucket).object(sourceObject).build();
            client.copyObject(CopyObjectArgs.builder().bucket(targetBucket).object(targetObject).source(source).build());
            log.debug("檔案複製成功：{}/{} 到 {}/{}", sourceBucket, sourceObject, targetBucket, targetObject);
        } catch (Exception e) {
            throw new FileStorageException("檔案複製發生錯誤: " + e.getMessage(), e);
        }
    }

    @Override
    public void cloneFiles(String bucket, List<CloneFilePairCommand> pairs) {
        List<CompletableFuture<Void>> futures = pairs.stream()
                .map(pair -> fileHelper.cloneFileAsynchronously(bucket, pair.sourceKey(), pair.targetKey()))
                .collect(Collectors.toList());
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("批次 clone 檔案完成，共 {} 筆", pairs.size());
    }

    @Override
    public void deleteFilesByPrefix(String bucket, String prefix) {
        try {
            if (StringUtils.isBlank(prefix)) {
                throw new FileStorageException("無法刪除無前綴之所有檔案");
            }
            Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                    .bucket(bucket).prefix(prefix).recursive(true).build());

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                futures.add(fileHelper.deleteFileAsynchronously(bucket, item.objectName()));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("以前綴 [{}] 刪除檔案完成，共: {} 筆", prefix, futures.size());
        } catch (Exception e) {
            log.error("以前綴 [{}] 刪除檔案失敗", prefix, e);
            throw new FileStorageException(e.getMessage(), e);
        }
    }

    // ==========================================
    // 新增：Pre-signed URL 支援
    // ==========================================

    @Override
    public String getPresignedUploadUrl(String bucket, String objectName, int expiryMinutes) throws Exception {
        return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .object(objectName)
                .expiry(expiryMinutes * 60)
                .build());
    }

    @Override
    public String getPresignedDownloadUrl(String bucket, String objectName, String originalFileName, boolean isDownload, int expiryMinutes) throws Exception {
        java.util.Map<String, String> extraQueryParams = new java.util.HashMap<>();
        
        String dispositionType = isDownload ? "attachment" : "inline";
        
        if (originalFileName != null && !originalFileName.isEmpty()) {
            // Encode the filename to handle spaces and non-ASCII characters correctly in headers
            String encodedFilename = java.net.URLEncoder.encode(originalFileName, java.nio.charset.StandardCharsets.UTF_8.toString()).replace("+", "%20");
            extraQueryParams.put("response-content-disposition", dispositionType + "; filename*=UTF-8''" + encodedFilename);
        } else {
            extraQueryParams.put("response-content-disposition", dispositionType);
        }

        return client.getPresignedObjectUrl(io.minio.GetPresignedObjectUrlArgs.builder()
                .method(io.minio.http.Method.GET)
                .bucket(bucket)
                .object(objectName)
                .expiry(expiryMinutes * 60)
                .extraQueryParams(extraQueryParams)
                .build());
    }

    // ==========================================
    // 新增：分段上傳 (Multipart Upload) 支援 (使用 AWS S3 相容 API)
    // ==========================================

    @Override
    public String initiateMultipartUpload(String bucket, String objectName) throws Exception {
        CreateMultipartUploadRequest request = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(request);
        return response.uploadId();
    }

    @Override
    public String getPresignedUploadPartUrl(String bucket, String objectName, String uploadId, int partNumber, int expiryMinutes) throws Exception {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .build();

        UploadPartPresignRequest presignRequest = UploadPartPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .uploadPartRequest(uploadPartRequest)
                .build();

        PresignedUploadPartRequest presignedRequest = s3Presigner.presignUploadPart(presignRequest);
        return presignedRequest.url().toString();
    }

    @Override
    public void completeMultipartUpload(String bucket, String objectName, String uploadId, Map<Integer, String> partETags) throws Exception {
        List<CompletedPart> completedParts = partETags.entrySet().stream()
                .map(entry -> CompletedPart.builder()
                        .partNumber(entry.getKey())
                        .eTag(entry.getValue())
                        .build())
                // 依照 partNumber 排序
                .sorted((p1, p2) -> p1.partNumber().compareTo(p2.partNumber()))
                .collect(Collectors.toList());

        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedParts)
                .build();

        CompleteMultipartUploadRequest request = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .uploadId(uploadId)
                .multipartUpload(completedMultipartUpload)
                .build();

        s3Client.completeMultipartUpload(request);
    }

    @Override
    public void abortMultipartUpload(String bucket, String objectName, String uploadId) throws Exception {
        AbortMultipartUploadRequest request = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(objectName)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(request);
    }
}

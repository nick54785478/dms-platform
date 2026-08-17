package com.example.demo.application.service;

import com.example.demo.application.shared.command.MultipartUploadCommand;
import com.example.demo.application.shared.dto.MultipartUploadInitiatedResult;
import com.example.demo.application.port.in.MultipartUploadUseCase;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 分段上傳應用服務 (Application Layer - Inbound Adapter)。
 * <p>
 * 實作 {@link MultipartUploadUseCase}，協調領域模型 (Domain) 與外部設施 (Outbound Port)
 * 進行分段上傳的初始化、分段網址產生、完成與放棄。
 * 並宣告為 package-private 以避免外層直接存取。
 * </p>
 */
@Service
class MultipartUploadService implements MultipartUploadUseCase {

    private final BlobStorageManagerPort blobStorageManagerPort;
    private final FileMetadataRepositoryPort fileMetadataRepositoryPort;
    private final String tempBucket;

    public MultipartUploadService(
            BlobStorageManagerPort blobStorageManagerPort,
            FileMetadataRepositoryPort fileMetadataRepositoryPort,
            @Value("${file-service.storage.temp-bucket}") String tempBucket) {
        this.blobStorageManagerPort = blobStorageManagerPort;
        this.fileMetadataRepositoryPort = fileMetadataRepositoryPort;
        this.tempBucket = tempBucket;
    }

    /**
     * 初始化分段上傳，取得 UploadId 並寫入初始之中繼資料。
     *
     * @param command 初始化分段上傳命令
     * @return 包含 FileId 與 UploadId 的結果 DTO
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public MultipartUploadInitiatedResult initiateMultipartUpload(MultipartUploadCommand.InitiateCommand command) throws Exception {
        // 1. Create FileMetadata (Domain)
        FileMetadata metadata = FileMetadata.create(
                command.tenantId(),
                command.originalFileName(),
                command.type(),
                command.mimeType(),
                command.size(),
                command.checksum(),
                command.tags()
        );

        // 2. Generate Object Key
        String objectKey = generateObjectKey(metadata.getTenantId(), metadata.getId(), metadata.getOriginalFileName());

        // 3. Initiate Multipart Upload via Port
        String uploadId = blobStorageManagerPort.initiateMultipartUpload(tempBucket, objectKey);

        // 4. Save Metadata to DB (It remains UNBOUND)
        fileMetadataRepositoryPort.save(metadata);

        return new MultipartUploadInitiatedResult(metadata.getId(), uploadId);
    }

    /**
     * 取得特定分段的預先簽名上傳網址。
     *
     * @param fileId        檔案 ID
     * @param uploadId      初始化時取得的 UploadId
     * @param partNumber    分段編號
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 該分段的上傳網址
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public String getPresignedPartUrl(String fileId, String uploadId, int partNumber, int expiryMinutes) throws Exception {
        FileMetadata metadata = fileMetadataRepositoryPort.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        String objectKey = generateObjectKey(metadata.getTenantId(), metadata.getId(), metadata.getOriginalFileName());

        return blobStorageManagerPort.getPresignedUploadPartUrl(tempBucket, objectKey, uploadId, partNumber, expiryMinutes);
    }

    /**
     * 完成分段上傳，將上傳的所有 Part 合併為單一檔案並更新中繼資料。
     *
     * @param command 完成分段上傳命令
     * @return 更新後的檔案中繼資料
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public FileMetadata completeMultipartUpload(MultipartUploadCommand.CompleteCommand command) throws Exception {
        FileMetadata metadata = fileMetadataRepositoryPort.findById(command.fileId())
                .orElseThrow(() -> new RuntimeException("File not found: " + command.fileId()));

        String objectKey = generateObjectKey(metadata.getTenantId(), metadata.getId(), metadata.getOriginalFileName());

        blobStorageManagerPort.completeMultipartUpload(tempBucket, objectKey, command.uploadId(), command.partETags());

        // In a complete flow, we might update size or check file validity here.
        // We save again if we modified metadata.
        return fileMetadataRepositoryPort.save(metadata);
    }

    /**
     * 放棄(中斷) 分段上傳，通知 Blob Storage 清除空間，並標示資料庫記錄為 DELETED。
     *
     * @param command 放棄分段上傳命令
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public void abortMultipartUpload(MultipartUploadCommand.AbortCommand command) throws Exception {
        FileMetadata metadata = fileMetadataRepositoryPort.findById(command.fileId())
                .orElseThrow(() -> new RuntimeException("File not found: " + command.fileId()));

        String objectKey = generateObjectKey(metadata.getTenantId(), metadata.getId(), metadata.getOriginalFileName());

        blobStorageManagerPort.abortMultipartUpload(tempBucket, objectKey, command.uploadId());

        // Optional: mark metadata as DELETED
        metadata.markAsDeleted();
        fileMetadataRepositoryPort.save(metadata);
    }

    private String generateObjectKey(String tenantId, String fileId, String originalFileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeTenantId = tenantId == null ? "default" : tenantId;
        return String.format("%s/%s/%s_%s", safeTenantId, datePath, fileId, originalFileName);
    }
}

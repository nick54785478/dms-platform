package com.example.demo.application.service;

import com.example.demo.application.shared.command.PresignedUrlCommand;
import com.example.demo.application.shared.command.UploadFileCommand;
import com.example.demo.application.shared.dto.PresignedUrlGeneratedResult;
import com.example.demo.application.port.in.ManageFileUseCase;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 檔案管理應用服務 (Application Layer - Inbound Adapter)。
 * <p>
 * 實作 {@link ManageFileUseCase}，協調領域模型 (Domain) 與外部設施 (Outbound Port)
 * 以執行小檔上傳、產生預先簽名網址等檔案相關核心業務，並宣告為 package-private 以避免外層直接存取。
 * </p>
 */
@Service
class FileManagementService implements ManageFileUseCase {

    private final BlobStorageManagerPort blobStorageManagerPort;
    private final FileMetadataRepositoryPort fileMetadataRepositoryPort;
    private final String tempBucket;
    private final String permanentBucket;

    public FileManagementService(
            BlobStorageManagerPort blobStorageManagerPort,
            FileMetadataRepositoryPort fileMetadataRepositoryPort,
            @Value("${file-service.storage.temp-bucket}") String tempBucket,
            @Value("${file-service.storage.permanent-bucket}") String permanentBucket) {
        this.blobStorageManagerPort = blobStorageManagerPort;
        this.fileMetadataRepositoryPort = fileMetadataRepositoryPort;
        this.tempBucket = tempBucket;
        this.permanentBucket = permanentBucket;
    }

    /**
     * 執行小檔案上傳。
     * 建立領域實體後將檔案寫入 Temp Bucket，並將中繼資料持久化至 DB。
     *
     * @param command 上傳命令
     * @return 建立完畢的中繼資料 (領域實體)
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public FileMetadata uploadFile(UploadFileCommand command) throws Exception {
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
        String objectKey = generateObjectKey(command.tenantId(), metadata.getId(), command.originalFileName());

        // 3. Upload File to temp bucket via Port
        blobStorageManagerPort.uploadFile(tempBucket, command.file(), "", objectKey);

        // 4. Save Metadata to DB
        return fileMetadataRepositoryPort.save(metadata);
    }

    /**
     * 產生預先簽名的上傳網址。
     * 建立初始的領域實體 (狀態為 UNBOUND)，並呼叫 BlobStorageManagerPort 產生上傳網址供 Client 使用。
     *
     * @param command 預先簽名網址命令
     * @return 包含 FileId 與網址的 DTO 結果
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public PresignedUrlGeneratedResult generatePresignedUploadUrl(PresignedUrlCommand command) throws Exception {
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
        String objectKey = generateObjectKey(command.tenantId(), metadata.getId(), command.originalFileName());

        // 3. Generate Pre-signed URL
        String url = blobStorageManagerPort.getPresignedUploadUrl(tempBucket, objectKey, command.expiryMinutes());

        // 4. Save Metadata to DB (It remains UNBOUND until business logic confirms it)
        fileMetadataRepositoryPort.save(metadata);

        return new PresignedUrlGeneratedResult(metadata.getId(), url);
    }

    /**
     * 產生預先簽名的下載網址。
     * 從儲存庫查詢該檔案是否存在，並產生下載專用網址。
     *
     * @param fileId 檔案 ID
     * @param expiryMinutes 網址有效時長 (分鐘)
     * @return 預先簽名的下載網址
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    public String generatePresignedDownloadUrl(String fileId, boolean isDownload, int expiryMinutes) throws Exception {
        // Find metadata (Need to handle Not Found in a real implementation)
        FileMetadata metadata = fileMetadataRepositoryPort.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        // Generate Object Key based on metadata
        String objectKey = generateObjectKey(metadata.getTenantId(), metadata.getId(), metadata.getOriginalFileName());

        // 根據檔案狀態決定要從哪個 Bucket 產生下載連結
        String targetBucket = metadata.getStatus() == com.example.demo.domain.file.aggregate.vo.FileStatus.BOUND 
                ? permanentBucket 
                : tempBucket;
                
        return blobStorageManagerPort.getPresignedDownloadUrl(targetBucket, objectKey, metadata.getOriginalFileName(), isDownload, expiryMinutes);
    }

    private String generateObjectKey(String tenantId, String fileId, String originalFileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeTenantId = tenantId == null ? "default" : tenantId;
        return String.format("%s/%s/%s_%s", safeTenantId, datePath, fileId, originalFileName);
    }
}

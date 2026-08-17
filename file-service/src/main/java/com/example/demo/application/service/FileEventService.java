package com.example.demo.application.service;

import com.example.demo.application.port.in.HandleFileEventUseCase;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.application.port.out.DistributedLockerPort;
import com.example.demo.application.shared.command.FileBoundCommand;
import com.example.demo.application.shared.command.FileDeletedCommand;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 檔案事件應用服務 (Application Layer - Inbound Adapter)。
 * <p>
 * 實作 {@link HandleFileEventUseCase}，負責協調領域模型 (Domain) 與外部設施介面 (Outbound Port)。
 * 處理檔案綁定 (FileBound) 與檔案刪除 (FileDeleted) 等非同步事件衍生的業務邏輯，
 * 並宣告為 package-private 以避免外層直接存取。
 * </p>
 */
@Service
class FileEventService implements HandleFileEventUseCase {

    private final FileMetadataRepositoryPort fileMetadataRepositoryPort;
    private final BlobStorageManagerPort blobStorageManagerPort;
    private final DistributedLockerPort distributedLockerPort;
    private final String tempBucket;
    private final String permanentBucket;

    public FileEventService(
            FileMetadataRepositoryPort fileMetadataRepositoryPort,
            BlobStorageManagerPort blobStorageManagerPort,
            DistributedLockerPort distributedLockerPort,
            @Value("${file-service.storage.temp-bucket}") String tempBucket,
            @Value("${file-service.storage.permanent-bucket}") String permanentBucket) {
        this.fileMetadataRepositoryPort = fileMetadataRepositoryPort;
        this.blobStorageManagerPort = blobStorageManagerPort;
        this.distributedLockerPort = distributedLockerPort;
        this.tempBucket = tempBucket;
        this.permanentBucket = permanentBucket;
    }

    /**
     * 執行檔案綁定業務流程。
     * 包含分散式鎖定、檔案狀態檢查、實際 Blob 搬移 (Temp -> Permanent)，與更新領域模型。
     *
     * @param command 檔案綁定命令
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    @Transactional
    public void handleFileBoundEvent(FileBoundCommand command) throws Exception {
        String lockKey = "file:lock:" + command.fileId();
        
        // Use watchdog to automatically extend the lock lease during potentially long file copying
        distributedLockerPort.runWithWatchdog(lockKey, 10, TimeUnit.SECONDS, () -> {
            FileMetadata metadata = fileMetadataRepositoryPort.findById(command.fileId())
                    .orElseThrow(() -> new RuntimeException("File not found: " + command.fileId()));

            if (metadata.getStatus() == FileStatus.BOUND) {
                return; // Already bound, idempotent operation
            }

            if (metadata.getStatus() == FileStatus.DELETED) {
                throw new RuntimeException("Cannot bind a deleted file: " + command.fileId());
            }

            // Generate object key
            String objectKey = generateObjectKey(metadata);

            try {
                // Move file from Temp to Permanent Bucket
                blobStorageManagerPort.cloneFile(tempBucket, objectKey, permanentBucket, objectKey);
                blobStorageManagerPort.deleteFile(tempBucket, "", objectKey);
            } catch (Exception e) {
                throw new RuntimeException("Failed to move file to permanent bucket", e);
            }

            // Update Domain Model
            metadata.markAsBound();
            fileMetadataRepositoryPort.save(metadata);
        });
    }

    /**
     * 執行檔案刪除業務流程。
     * 包含分散式鎖定、檢查 Blob 所在 Bucket，實際刪除檔案，以及標記領域模型為已刪除 (Soft Delete)。
     *
     * @param command 檔案刪除命令
     * @throws Exception 處理過程中發生的例外
     */
    @Override
    @Transactional
    public void handleFileDeletedEvent(FileDeletedCommand command) throws Exception {
        String lockKey = "file:lock:" + command.fileId();

        distributedLockerPort.runWithWatchdog(lockKey, 10, TimeUnit.SECONDS, () -> {
            FileMetadata metadata = fileMetadataRepositoryPort.findById(command.fileId())
                    .orElseThrow(() -> new RuntimeException("File not found: " + command.fileId()));

            if (metadata.getStatus() == FileStatus.DELETED) {
                return; // Already deleted, idempotent operation
            }

            String objectKey = generateObjectKey(metadata);

            // Delete from the appropriate bucket based on its current status
            String targetBucket = (metadata.getStatus() == FileStatus.BOUND) ? permanentBucket : tempBucket;
            
            try {
                blobStorageManagerPort.deleteFile(targetBucket, "", objectKey);
            } catch (Exception e) {
                // Log warning but continue to update DB status (Soft Delete fallback)
                System.err.println("Warning: Could not physically delete file from bucket: " + e.getMessage());
            }

            metadata.markAsDeleted();
            fileMetadataRepositoryPort.save(metadata);
        });
    }

    private String generateObjectKey(FileMetadata metadata) {
        String datePath = metadata.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeTenantId = metadata.getTenantId() == null ? "default" : metadata.getTenantId();
        return String.format("%s/%s/%s_%s", safeTenantId, datePath, metadata.getId(), metadata.getOriginalFileName());
    }
}

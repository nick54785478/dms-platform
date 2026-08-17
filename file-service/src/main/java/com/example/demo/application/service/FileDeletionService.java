package com.example.demo.application.service;

import com.example.demo.application.port.in.DeleteFileUseCase;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
class FileDeletionService implements DeleteFileUseCase {

    private final FileMetadataRepositoryPort fileMetadataRepositoryPort;
    private final BlobStorageManagerPort blobStorageManagerPort;
    private final String permanentBucket;
    private final String tempBucket;

    public FileDeletionService(
            FileMetadataRepositoryPort fileMetadataRepositoryPort,
            BlobStorageManagerPort blobStorageManagerPort,
            @Value("${file-service.storage.permanent-bucket}") String permanentBucket,
            @Value("${file-service.storage.temp-bucket}") String tempBucket) {
        this.fileMetadataRepositoryPort = fileMetadataRepositoryPort;
        this.blobStorageManagerPort = blobStorageManagerPort;
        this.permanentBucket = permanentBucket;
        this.tempBucket = tempBucket;
    }

    @Override
    public void deleteFile(String fileId) throws Exception {
        log.info("Processing deletion for fileId: {}", fileId);
        
        FileMetadata metadata = fileMetadataRepositoryPort.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        if (metadata.getStatus() == FileStatus.DELETED) {
            log.info("File {} is already DELETED. Ignoring.", fileId);
            return;
        }

        boolean wasBound = metadata.getStatus() == FileStatus.BOUND;
        
        // 更新狀態為 DELETED
        metadata.markAsDeleted();
        fileMetadataRepositoryPort.save(metadata);
        
        String objectKey;
        if (wasBound) {
            // 如果已綁定，檔案在 permanentBucket，路徑包含日期
            objectKey = generateObjectKey(metadata);
            log.info("Deleting file {} from permanent bucket {}", objectKey, permanentBucket);
            blobStorageManagerPort.deleteFile(permanentBucket, "", objectKey);
        } else {
            // 如果還沒綁定，檔案在 tempBucket，路徑只有檔名
            objectKey = metadata.getOriginalFileName();
            log.info("Deleting file {} from temp bucket {}", objectKey, tempBucket);
            blobStorageManagerPort.deleteFile(tempBucket, "", objectKey);
        }
        
        log.info("Successfully deleted file {}", fileId);
    }

    private String generateObjectKey(FileMetadata metadata) {
        // 利用最後更新時間(通常是綁定時間)來推算 objectKey
        String datePath = metadata.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeTenantId = metadata.getTenantId() == null ? "default" : metadata.getTenantId();
        return String.format("%s/%s/%s_%s", safeTenantId, datePath, metadata.getId(), metadata.getOriginalFileName());
    }
}

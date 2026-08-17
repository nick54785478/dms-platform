package com.example.demo.application.service;

import com.example.demo.application.port.in.BindFileUseCase;
import com.example.demo.application.port.out.BlobStorageManagerPort;
import com.example.demo.application.port.out.FileMetadataRepositoryPort;
import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
class FileBindingService implements BindFileUseCase {

    private final FileMetadataRepositoryPort fileMetadataRepositoryPort;
    private final BlobStorageManagerPort blobStorageManagerPort;
    private final String tempBucket;
    private final String permanentBucket;

    public FileBindingService(
            FileMetadataRepositoryPort fileMetadataRepositoryPort,
            BlobStorageManagerPort blobStorageManagerPort,
            @Value("${file-service.storage.temp-bucket}") String tempBucket,
            @Value("${file-service.storage.permanent-bucket}") String permanentBucket) {
        this.fileMetadataRepositoryPort = fileMetadataRepositoryPort;
        this.blobStorageManagerPort = blobStorageManagerPort;
        this.tempBucket = tempBucket;
        this.permanentBucket = permanentBucket;
    }

    @Override
    public void bindFile(String fileId) throws Exception {
        log.info("Processing binding for fileId: {}", fileId);
        
        FileMetadata metadata = fileMetadataRepositoryPort.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found: " + fileId));

        if (metadata.getStatus() == FileStatus.BOUND) {
            log.info("File {} is already BOUND. Ignoring.", fileId);
            return;
        }

        String objectKey = generateObjectKey(metadata.getTenantId(), metadata.getId(), metadata.getOriginalFileName());

        try {
            log.info("Cloning file {} from {} to {}", objectKey, tempBucket, permanentBucket);
            // 複製到永久區
            blobStorageManagerPort.cloneFile(tempBucket, objectKey, permanentBucket, objectKey);
            
            log.info("Deleting file {} from temp bucket {}", objectKey, tempBucket);
            // 刪除暫存區檔案 (filePath is empty, fileName is the objectKey)
            blobStorageManagerPort.deleteFile(tempBucket, "", objectKey);
            
            // 更新狀態
            metadata.markAsBound();
            fileMetadataRepositoryPort.save(metadata);
            
            log.info("Successfully bound file {}", fileId);
        } catch (Exception e) {
            log.error("Error during file binding process for file {}", fileId, e);
            throw e;
        }
    }

    private String generateObjectKey(String tenantId, String fileId, String originalFileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String safeTenantId = tenantId == null ? "default" : tenantId;
        return String.format("%s/%s/%s_%s", safeTenantId, datePath, fileId, originalFileName);
    }
}

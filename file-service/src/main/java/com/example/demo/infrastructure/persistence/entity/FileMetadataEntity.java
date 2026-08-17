package com.example.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.domain.file.aggregate.vo.FileStatus;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 檔案中介資料的 JPA 實體 (Entity)
 * 負責與資料庫 table `file_metadata` 進行映射 (ORM)，屬於 Infrastructure Layer
 */
@Getter
@Entity
@Table(name = "file_metadata")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMetadataEntity {

    /**
     * 實體的主鍵 (對應 Domain 的 ID)
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /**
     * 租戶 ID，用於多租戶架構下的資料隔離
     */
    @Column(name = "tenant_id")
    private String tenantId;

    /**
     * 原始檔案名稱
     */
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    /**
     * 檔案的 MIME 類型 (例如: image/png, application/pdf)
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * 檔案大小 (以 Bytes 為單位)
     */
    @Column(name = "size")
    private Long size;

    /**
     * 檔案校驗碼，用於驗證檔案完整性
     */
    @Column(name = "checksum")
    private String checksum;

    /**
     * 檔案分類類型
     */
    @Column(name = "type")
    private String type;

    /**
     * 檔案的自訂標籤，對應資料庫中的 JSONB 欄位
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, String> tags;

    /**
     * 檔案狀態的字串表示 (例如: "UNBOUND", "BOUND", "DELETED")
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * 資料建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 資料最後更新時間
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 從 Domain Model 轉換為 JPA Entity
     *
     * @param domain 領域實體 (FileMetadata)
     * @return 轉換後的 JPA 實體 (FileMetadataEntity)
     */
    public static FileMetadataEntity create(FileMetadata domain) {
        if (domain == null) return null;
        FileMetadataEntity entity = new FileMetadataEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId();
        entity.originalFileName = domain.getOriginalFileName();
        entity.mimeType = domain.getMimeType();
        entity.size = domain.getSize();
        entity.checksum = domain.getChecksum();
        entity.type = domain.getType();
        entity.tags = domain.getTags();
        if (domain.getStatus() != null) {
            entity.status = domain.getStatus().name();
        }
        entity.createdAt = domain.getCreatedAt();
        entity.updatedAt = domain.getUpdatedAt();
        return entity;
    }

    /**
     * 從 JPA Entity 轉換回 Domain Model
     *
     * @return 轉換後的領域實體 (FileMetadata)
     */
    public FileMetadata toDomain() {
        FileStatus fileStatus = null;
        if (this.status != null) {
            fileStatus = FileStatus.valueOf(this.status);
        }
        return FileMetadata.builder()
                .id(this.id)
                .tenantId(this.tenantId)
                .originalFileName(this.originalFileName)
                .mimeType(this.mimeType)
                .size(this.size)
                .checksum(this.checksum)
                .type(this.type)
                .tags(this.tags)
                .status(fileStatus)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}

package com.example.demo.domain.file.aggregate.root;

import com.example.demo.domain.file.aggregate.vo.FileStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 檔案中介資料 (Metadata) 的領域實體 (Aggregate Root)
 * 完全業務無知，只關心檔案本身的狀態。
 * 遵循 Pure Domain 原則，不使用 Lombok 與任何框架註解。
 */
public class FileMetadata {
    
    /**
     * 檔案唯一識別碼 (通常為 UUID)
     */
    private String id;

    /**
     * 原始檔案名稱
     */
    private String originalFileName;

    /**
     * 檔案的 MIME 類型 (例如: image/png, application/pdf)
     */
    private String mimeType;

    /**
     * 檔案大小 (以 Bytes 為單位)
     */
    private Long size;

    /**
     * 檔案校驗碼 (例如: SHA-256 或 MD5)，用於驗證檔案完整性
     */
    private String checksum;

    /**
     * 檔案分類類型 (例如: image, document, video)
     */
    private String type;

    /**
     * 租戶 ID，用於多租戶架構下的資料隔離
     */
    private String tenantId;

    /**
     * 檔案的自訂標籤 (Key-Value 格式)
     */
    private Map<String, String> tags;

    /**
     * 檔案狀態 (UNBOUND, BOUND, DELETED)
     */
    private FileStatus status;

    /**
     * 檔案建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 檔案最後更新時間
     */
    private LocalDateTime updatedAt;

    // 給 Repository 從資料庫重建 (Reconstitute) 實體時使用 Builder
    private FileMetadata(Builder builder) {
        this.id = builder.id;
        this.originalFileName = builder.originalFileName;
        this.mimeType = builder.mimeType;
        this.size = builder.size;
        this.checksum = builder.checksum;
        this.type = builder.type;
        this.tenantId = builder.tenantId;
        this.tags = builder.tags;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String originalFileName;
        private String mimeType;
        private Long size;
        private String checksum;
        private String type;
        private String tenantId;
        private Map<String, String> tags = new HashMap<>();
        private FileStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder originalFileName(String originalFileName) {
            this.originalFileName = originalFileName;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder tags(Map<String, String> tags) {
            if (tags != null) {
                this.tags = new HashMap<>(tags);
            }
            return this;
        }

        public Builder status(FileStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FileMetadata build() {
            return new FileMetadata(this);
        }
    }

    /**
     * 商業邏輯：創建全新的檔案中介資料 (預設狀態為 UNBOUND)
     *
     * @param tenantId         租戶 ID
     * @param originalFileName 原始檔案名稱
     * @param type             檔案分類類型
     * @param mimeType         MIME 類型
     * @param size             檔案大小
     * @param checksum         檔案校驗碼
     * @param tags             自訂標籤
     * @return 創建完成的新檔案中介資料實體
     */
    public static FileMetadata create(String tenantId, String originalFileName, String type, String mimeType, Long size, String checksum, Map<String, String> tags) {
        LocalDateTime now = LocalDateTime.now();
        return builder()
                .id(UUID.randomUUID().toString())
                .tenantId(tenantId)
                .originalFileName(originalFileName)
                .type(type)
                .mimeType(mimeType)
                .size(size)
                .checksum(checksum)
                .tags(tags)
                .status(FileStatus.UNBOUND) // 新上傳的檔案預設為未綁定
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 商業邏輯：標記檔案為已綁定狀態 (表示業務端已傳送 FileBoundEvent 認領)
     * 若檔案已被標記為刪除，則不允許綁定並拋出例外。
     *
     * @throws IllegalStateException 若檔案狀態為已刪除
     */
    public void markAsBound() {
        if (this.status == FileStatus.DELETED) {
            throw new IllegalStateException("無法綁定已刪除的檔案");
        }
        this.status = FileStatus.BOUND;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 商業邏輯：標記檔案為已刪除狀態
     */
    public void markAsDeleted() {
        this.status = FileStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 商業邏輯：標記檔案為上傳完成但未綁定 (Temp 狀態)
     */
    public void markAsUnbound() {
        this.status = FileStatus.UNBOUND;
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // Getters
    // ==========================================

    /**
     * 取得檔案唯一識別碼
     *
     * @return 檔案 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 取得原始檔案名稱
     *
     * @return 原始檔案名稱
     */
    public String getOriginalFileName() {
        return originalFileName;
    }

    /**
     * 取得檔案的 MIME 類型
     *
     * @return MIME 類型
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * 取得檔案大小
     *
     * @return 檔案大小 (Bytes)
     */
    public Long getSize() {
        return size;
    }

    /**
     * 取得檔案校驗碼
     *
     * @return 檔案校驗碼
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * 取得檔案分類類型
     *
     * @return 檔案類型
     */
    public String getType() {
        return type;
    }

    /**
     * 取得租戶 ID
     *
     * @return 租戶 ID
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * 取得檔案的自訂標籤 (回傳不可修改的 Map 以保護封裝)
     *
     * @return 標籤 Map
     */
    public Map<String, String> getTags() {
        return tags == null ? Collections.emptyMap() : Collections.unmodifiableMap(tags);
    }

    /**
     * 取得檔案狀態
     *
     * @return 檔案狀態
     */
    public FileStatus getStatus() {
        return status;
    }

    /**
     * 取得檔案建立時間
     *
     * @return 建立時間
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 取得檔案最後更新時間
     *
     * @return 更新時間
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

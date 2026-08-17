package com.example.dms.domain.document.aggregate.entity;

import com.example.dms.domain.document.aggregate.vo.DocumentVersionId;

import java.time.LocalDateTime;

/**
 * 文件版本實體 (Entity within Aggregate)
 * 作為 Document 每次變更時留下的不可變歷史快照
 */
public class DocumentVersion {

    private final DocumentVersionId id;
    private final Integer majorVersion;
    private final Integer minorVersion;
    private final String title;
    private final String description;
    private final String fileId;
    private final LocalDateTime createdAt;

    /**
     * 給 Repository 重建用的 Constructor
     */
    public DocumentVersion(DocumentVersionId id, Integer majorVersion, Integer minorVersion, String title, String description, String fileId, LocalDateTime createdAt) {
        this.id = id;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.title = title;
        this.description = description;
        this.fileId = fileId;
        this.createdAt = createdAt;
    }

    /**
     * 建立新的版本快照
     */
    public static DocumentVersion createSnapshot(Integer majorVersion, Integer minorVersion, String title, String description, String fileId) {
        return new DocumentVersion(
                DocumentVersionId.generate(),
                majorVersion,
                minorVersion,
                title,
                description,
                fileId,
                LocalDateTime.now()
        );
    }

    public DocumentVersionId getId() {
        return id;
    }

    public Integer getMajorVersion() {
        return majorVersion;
    }

    public Integer getMinorVersion() {
        return minorVersion;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFileId() {
        return fileId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 格式化輸出語意化版本號 (例如 V1.0, V0.1)
     */
    public String getSemanticVersion() {
        return "V" + majorVersion + "." + minorVersion;
    }
}

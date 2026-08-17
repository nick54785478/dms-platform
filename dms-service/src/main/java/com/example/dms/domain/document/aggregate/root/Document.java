package com.example.dms.domain.document.aggregate.root;

import com.example.dms.domain.document.aggregate.entity.DocumentVersion;
import com.example.dms.domain.document.aggregate.vo.DocumentId;
import com.example.dms.domain.document.aggregate.vo.DocumentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件聚合根 (Aggregate Root)
 * 封裝文件的核心屬性與商業邏輯狀態
 */
public class Document {

    /**
     * 文件唯一識別碼
     */
    private final DocumentId id;

    /**
     * 文件標題
     */
    private String title;

    /**
     * 文件描述
     */
    private String description;

    /**
     * 實體檔案的 ID，對應 file-service 的 fileId
     */
    private String fileId;

    /**
     * 文件的目前狀態
     */
    private DocumentStatus status;

    /**
     * 文件建立時間
     */
    private final LocalDateTime createdAt;

    /**
     * 文件最後更新時間
     */
    private LocalDateTime updatedAt;

    /**
     * 主版本號
     */
    private Integer majorVersion;

    /**
     * 次版本號
     */
    private Integer minorVersion;

    /**
     * 歷史版本快照
     */
    private final List<DocumentVersion> history;

    /**
     * 給 Repository 重建用的 Constructor
     *
     * @param id           文件唯一識別碼
     * @param title        文件標題
     * @param description  文件描述
     * @param fileId       實體檔案 ID
     * @param status       文件狀態
     * @param createdAt    建立時間
     * @param updatedAt    更新時間
     * @param majorVersion 主版本號
     * @param minorVersion 次版本號
     * @param history      歷史版本清單
     */
    private Document(DocumentId id, String title, String description, String fileId, DocumentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, Integer majorVersion, Integer minorVersion, List<DocumentVersion> history) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileId = fileId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.history = new ArrayList<>(history != null ? history : Collections.emptyList());
    }

    /**
     * 僅供 Infrastructure (Repository) 從資料庫還原 Aggregate 狀態時使用。
     * 嚴禁在 Application Layer 建立新物件時呼叫此方法。
     */
    public static Document reconstitute(DocumentId id, String title, String description, String fileId, DocumentStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, Integer majorVersion, Integer minorVersion, List<DocumentVersion> history) {
        return new Document(id, title, description, fileId, status, createdAt, updatedAt, majorVersion, minorVersion, history);
    }

    /**
     * 商業邏輯：建立新文件
     *
     * @param title       文件標題
     * @param description 文件描述
     * @param fileId      實體檔案 ID
     * @return 建立完成的新文件聚合根實體，初始狀態為 DRAFT
     */
    public static Document create(String title, String description, String fileId) {
        LocalDateTime now = LocalDateTime.now();
        Document document = new Document(
                DocumentId.generate(),
                title,
                description,
                fileId,
                DocumentStatus.DRAFT,
                now,
                now,
                0,
                1,
                new ArrayList<>()
        );
        document.addSnapshot();
        return document;
    }

    private void addSnapshot() {
        this.history.add(DocumentVersion.createSnapshot(
                this.majorVersion,
                this.minorVersion,
                this.title,
                this.description,
                this.fileId
        ));
    }

    /**
     * 商業邏輯：更新文件資訊
     * 若文件狀態為 ARCHIVED 或 DELETED，則不允許更新並拋出例外。
     *
     * @param title       新的文件標題
     * @param description 新的文件描述
     * @throws IllegalStateException 若文件狀態為已封存或已刪除
     */
    public void update(String title, String description) {
        if (this.status == DocumentStatus.ARCHIVED || this.status == DocumentStatus.DELETED) {
            throw new IllegalStateException("無法更新已封存或刪除的文件");
        }
        this.title = title;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
        this.minorVersion++;
        this.addSnapshot();
    }

    /**
     * 商業邏輯：上傳新版本文件 (綁定新實體檔案)
     * 若文件狀態為 ARCHIVED 或 DELETED，則不允許上傳並拋出例外。
     *
     * @param fileId         新的實體檔案 ID
     * @param isMajorVersion 是否為主版本更新
     * @throws IllegalStateException 若文件狀態為已封存或已刪除
     */
    public void uploadNewVersion(String fileId, boolean isMajorVersion) {
        if (this.status == DocumentStatus.ARCHIVED || this.status == DocumentStatus.DELETED) {
            throw new IllegalStateException("無法更新已封存或刪除的文件");
        }
        this.fileId = fileId;
        this.updatedAt = LocalDateTime.now();
        if (isMajorVersion) {
            this.majorVersion++;
            this.minorVersion = 0;
        } else {
            this.minorVersion++;
        }
        this.addSnapshot();
    }

    /**
     * 商業邏輯：發布文件
     * 若文件已刪除，則無法發布。發布後文件狀態變更為 PUBLISHED。
     *
     * @throws IllegalStateException 若文件狀態為已刪除
     */
    public void publish() {
        if (this.status == DocumentStatus.DELETED) {
            throw new IllegalStateException("無法發布已刪除的文件");
        }
        this.status = DocumentStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
        this.majorVersion++;
        this.minorVersion = 0;
        this.addSnapshot();
    }

    /**
     * 商業邏輯：刪除文件
     * 若文件已刪除，則不執行任何動作。
     */
    public void delete() {
        if (this.status == DocumentStatus.DELETED) {
            return;
        }
        this.status = DocumentStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取得文件唯一識別碼
     *
     * @return 文件 ID
     */
    public DocumentId getId() {
        return id;
    }

    /**
     * 取得文件標題
     *
     * @return 文件標題
     */
    public String getTitle() {
        return title;
    }

    /**
     * 取得文件描述
     *
     * @return 文件描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 取得實體檔案的 ID
     *
     * @return 實體檔案 ID
     */
    public String getFileId() {
        return fileId;
    }

    /**
     * 取得文件的目前狀態
     *
     * @return 文件狀態
     */
    public DocumentStatus getStatus() {
        return status;
    }

    /**
     * 取得文件建立時間
     *
     * @return 建立時間
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 取得文件最後更新時間
     *
     * @return 更新時間
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 取得主版本號
     *
     * @return 主版本號
     */
    public Integer getMajorVersion() {
        return majorVersion;
    }

    /**
     * 取得次版本號
     *
     * @return 次版本號
     */
    public Integer getMinorVersion() {
        return minorVersion;
    }

    /**
     * 取得語意化版本號字串 (e.g. V1.0, V0.1)
     */
    public String getSemanticVersion() {
        return "V" + majorVersion + "." + minorVersion;
    }

    /**
     * 取得唯讀的歷史版本清單
     *
     * @return 歷史版本
     */
    public List<DocumentVersion> getHistory() {
        return Collections.unmodifiableList(history);
    }
}

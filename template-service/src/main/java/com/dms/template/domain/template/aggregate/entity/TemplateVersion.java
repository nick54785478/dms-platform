package com.dms.template.domain.template.aggregate.entity;

import com.dms.template.domain.template.aggregate.vo.TemplateStatus;
import com.dms.template.domain.template.aggregate.vo.TemplateVariable;

import java.util.Collections;
import java.util.List;

/**
 * 範本的具體版本實體 (Entity)
 * <p>
 * 在 DDD 架構中，此類別為 {@link com.dms.template.domain.template.aggregate.root.Template} 聚合根底下的內部實體 (Entity)。
 * 負責維護單一版本的內容定義、狀態流轉 (如草稿、發布、封存) 與變數清單。
 * 嚴守零框架依賴原則，不使用 Lombok 或任何框架註解。
 * </p>
 */
public class TemplateVersion {
    private String version;
    private String contentDefinition;
    private TemplateStatus status;
    private List<TemplateVariable> variables;

    private TemplateVersion(String version, String contentDefinition, TemplateStatus status, List<TemplateVariable> variables) {
        this.version = version;
        this.contentDefinition = contentDefinition;
        this.status = status;
        this.variables = variables;
    }

    /**
     * 工廠方法：建立一個全新的草稿 (DRAFT) 範本版本
     *
     * @param version           版本號 (例如 "1.0-DRAFT")
     * @param contentDefinition 範本實體的二進位內容定義
     * @param variables         該版本內解析出的變數清單
     * @return 新建立的 {@link TemplateVersion} 實體
     */
    public static TemplateVersion create(String version, String contentDefinition, List<TemplateVariable> variables) {
        return new TemplateVersion(version, contentDefinition, TemplateStatus.DRAFT, variables);
    }
    
    /**
     * 工廠方法：從資料庫重建 (Reconstitute) 既有的範本版本實體
     *
     * @param version           版本號
     * @param contentDefinition 範本內容定義
     * @param status            當前版本狀態
     * @param variables         變數清單
     * @return 重建後的 {@link TemplateVersion} 實體
     */
    public static TemplateVersion reconstitute(String version, String contentDefinition, TemplateStatus status, List<TemplateVariable> variables) {
        return new TemplateVersion(version, contentDefinition, status, variables);
    }
    
    /**
     * 更新草稿版本的內容與變數清單
     *
     * @param contentDefinition 新的範本內容定義
     * @param variables         新的變數清單
     * @throws IllegalStateException 若當前版本狀態非草稿 (DRAFT) 則拋出例外
     */
    public void updateContent(String contentDefinition, List<TemplateVariable> variables) {
        if (this.status != TemplateStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT version can be updated");
        }
        this.contentDefinition = contentDefinition;
        this.variables = variables;
    }

    /**
     * 將當前草稿版本發布上架，狀態變更為 PUBLISHED，並移除版本號中的 "-DRAFT" 後綴
     */
    public void publish() {
        this.status = TemplateStatus.PUBLISHED;
        if (this.version.endsWith("-DRAFT")) {
            this.version = this.version.replace("-DRAFT", "");
        }
    }

    /**
     * 將當前版本封存下架，狀態變更為 ARCHIVED
     */
    public void archive() {
        this.status = TemplateStatus.ARCHIVED;
    }

    /**
     * 取得版本號
     *
     * @return 版本號字串
     */
    public String getVersion() {
        return version;
    }

    /**
     * 取得範本的實體內容定義
     *
     * @return 內容定義字串 (通常是 Base64 編碼的檔案內容)
     */
    public String getContentDefinition() {
        return contentDefinition;
    }

    /**
     * 取得當前範本版本的狀態
     *
     * @return 範本狀態 {@link TemplateStatus}
     */
    public TemplateStatus getStatus() {
        return status;
    }

    /**
     * 取得該版本中定義的所有變數清單，回傳唯讀集合以保護領域內部狀態
     *
     * @return 不可變的變數清單集合 {@link TemplateVariable}
     */
    public List<TemplateVariable> getVariables() {
        return Collections.unmodifiableList(variables);
    }
}

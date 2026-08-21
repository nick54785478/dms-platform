package com.dms.template.domain.template.aggregate.root;

import com.dms.template.domain.template.aggregate.entity.TemplateVersion;
import com.dms.template.domain.template.aggregate.vo.TemplateId;
import com.dms.template.domain.template.aggregate.vo.TemplateStatus;
import com.dms.template.domain.template.aggregate.vo.TemplateVariable;
import com.dms.template.domain.template.aggregate.vo.TemplateType;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 範本聚合根 (Template Aggregate Root).
 *
 * <p>
 * 在領域驅動設計 (DDD) 中，此類別為「範本 (Template)」領域的聚合根。
 * 它負責封裝範本的核心屬性與商業邏輯，並管理其內部的版本實體 ({@link TemplateVersion})。
 * 此類別為純 Java (Pure Java) 實作，絕不依賴任何外部框架 (如 Spring, JPA 等) 或資料庫技術細節。
 * </p>
 */
@Getter
public class Template {
    /** 聚合根的唯一識別碼 (Value Object) */
    private final TemplateId id;

    /** 範本的類型 (如: EXCEL, WORD, PDF 等) */
    private final TemplateType templateType;

    /** 範本的專屬代碼 (通常用於外部系統對接識別) */
    private final String templateCode;

    /** 範本的顯示名稱 */
    private String name;

    /** 範本的詳細描述 */
    private String description;

    /** 此範本所擁有的各個版本集合 (內部實體) */
    private final List<TemplateVersion> versions;

    private Template(TemplateId id, TemplateType templateType, String templateCode, String name, String description, List<TemplateVersion> versions) {
        this.id = id;
        this.templateType = templateType;
        this.templateCode = templateCode;
        this.name = name;
        this.description = description;
        this.versions = new ArrayList<>(versions != null ? versions : Collections.emptyList());
    }

    private Template(TemplateId id, TemplateType templateType, String templateCode, String name, String description) {
        this(id, templateType, templateCode, name, description, new ArrayList<>());
    }

    /**
     * 建立全新的範本聚合根實體 (Factory Method).
     *
     * <p>會自動產生全新的 {@link TemplateId} 作為唯一識別碼，
     * 通常用於初次建立範本的 UseCase 中。</p>
     *
     * @param templateType 範本類型
     * @param templateCode 範本代碼
     * @param name         範本名稱
     * @param description  範本描述
     * @return 新建立的範本聚合根
     */
    public static Template create(TemplateType templateType, String templateCode, String name, String description) {
        return new Template(TemplateId.generate(), templateType, templateCode, name, description);
    }

    /**
     * 從儲存庫 (Repository) 重建聚合根實體 (Reconstitute).
     *
     * <p>
     * 將資料庫中的資料轉化回領域模型，與 {@link #create} 的不同之處在於，
     * 此方法直接使用既有的 {@link TemplateId}，且不會觸發或發布任何領域事件 (Domain Events)。
     * </p>
     *
     * @param id           既有的識別碼
     * @param templateType 範本類型
     * @param templateCode 範本代碼
     * @param name         範本名稱
     * @param description  範本描述
     * @param versions     既有的版本集合
     * @return 重建後的範本聚合根
     */
    public static Template reconstitute(TemplateId id, TemplateType templateType, String templateCode, String name, String description, List<TemplateVersion> versions) {
        return new Template(id, templateType, templateCode, name, description, versions);
    }

    /**
     * 新增一個範本版本 (TemplateVersion).
     *
     * @param version           版本號 (例如: "V1.0")
     * @param contentDefinition 範本內容定義 (通常為 JSON 字串)
     * @param variables         範本內所使用的變數清單
     */
    public void addVersion(String version, String contentDefinition, List<TemplateVariable> variables) {
        TemplateVersion newVersion = TemplateVersion.create(version, contentDefinition, variables);
        this.versions.add(newVersion);
    }
    
    /**
     * 儲存草稿版本的領域邏輯.
     *
     * <p>
     * 會先檢查目前的版本集合中是否已存在狀態為 {@code DRAFT} 的版本。
     * 若存在，則直接更新該草稿的內容與變數；
     * 若不存在，則自動建立一個初始版號 (如: "V1.0-DRAFT") 的新版本。
     * </p>
     *
     * @param contentDefinition 欲儲存的範本內容定義 (JSON 字串)
     * @param variables         對應的範本變數清單
     */
    public void saveDraft(String contentDefinition, List<TemplateVariable> variables) {
        // 尋找目前是否已有草稿版本
        Optional<TemplateVersion> draftOpt = this.versions.stream()
                .filter(v -> v.getStatus() == TemplateStatus.DRAFT)
                .findFirst();
                
        if (draftOpt.isPresent()) {
            draftOpt.get().updateContent(contentDefinition, variables);
        } else {
            // 如果沒有，自動建立 V1.0-DRAFT 作為初始版本
            this.addVersion("V1.0-DRAFT", contentDefinition, variables);
        }
    }

    /**
     * 發佈指定版號的範本.
     *
     * <p>
     * 在版本清單中尋找對應的版號，若找到則將其狀態變更為發佈 (PUBLISHED)。
     * 若找不到指定版號，將拋出 {@link IllegalArgumentException}。
     * </p>
     *
     * @param version 欲發佈的版本號
     * @throws IllegalArgumentException 當找不到指定的版號時拋出
     */
    public void publishVersion(String version) {
        Optional<TemplateVersion> targetVersion = versions.stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst();

        if (targetVersion.isPresent()) {
            targetVersion.get().publish();
        } else {
            throw new IllegalArgumentException("Version not found: " + version);
        }
    }

    public List<TemplateVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }
}

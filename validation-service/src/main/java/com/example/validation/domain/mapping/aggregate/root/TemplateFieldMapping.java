package com.example.validation.domain.mapping.aggregate.root;

/**
 * 範本欄位對應 (Aggregate Root)
 * <p>
 * 負責維護範本上傳時，實際的 Header 名稱與 ValidationPolicy 所需的 mappingFieldName 之間的對應關係。
 */
public class TemplateFieldMapping {

    /**
     * 對應紀錄的唯一識別碼 (Primary Key)
     */
    private Long id;

    /**
     * 對應範本的唯一代碼
     */
    private String templateCode;

    /**
     * 範本內的 Sheet 頁籤名稱 (Excel 適用)
     */
    private String templateSheetName;

    /**
     * 上傳範本時，使用者實際上看到的欄位標題 (例如: "員工姓名")
     */
    private String headerName;

    /**
     * 系統內部 ValidationPolicy 綁定驗證規則時使用的對應名稱 (例如: "NAME")
     */
    private String mappingFieldName;

    /**
     * 私有建構子，由靜態工廠方法呼叫
     */
    private TemplateFieldMapping(Long id, String templateCode, String templateSheetName, String headerName, String mappingFieldName) {
        this.id = id;
        this.templateCode = templateCode;
        this.templateSheetName = templateSheetName;
        this.headerName = headerName;
        this.mappingFieldName = mappingFieldName;
    }

    /**
     * 從資料庫重新建構聚合根實體 (非新建行為)
     *
     * @param id                實體 ID
     * @param templateCode      範本唯一代碼
     * @param templateSheetName 範本內的頁籤名稱
     * @param headerName        原始欄位標題
     * @param mappingFieldName  內部對應名稱
     * @return 重新建構的 TemplateFieldMapping 實體
     */
    public static TemplateFieldMapping reconstitute(Long id, String templateCode, String templateSheetName, String headerName, String mappingFieldName) {
        return new TemplateFieldMapping(id, templateCode, templateSheetName, headerName, mappingFieldName);
    }

    /**
     * 建立全新的 TemplateFieldMapping 實體 (業務行為)
     * 
     * @param templateCode      範本唯一代碼
     * @param templateSheetName 範本內的頁籤名稱
     * @param headerName        原始欄位標題
     * @param mappingFieldName  內部對應名稱
     * @return 全新的 TemplateFieldMapping 實體 (無 ID)
     */
    public static TemplateFieldMapping create(String templateCode, String templateSheetName, String headerName, String mappingFieldName) {
        return new TemplateFieldMapping(null, templateCode, templateSheetName, headerName, mappingFieldName);
    }

    /**
     * 更新 TemplateFieldMapping 的欄位屬性 (業務行為)
     *
     * @param templateCode      新的範本唯一代碼
     * @param templateSheetName 新的頁籤名稱
     * @param headerName        新的欄位標題
     * @param mappingFieldName  新的內部對應名稱
     */
    public void update(String templateCode, String templateSheetName, String headerName, String mappingFieldName) {
        this.templateCode = templateCode;
        this.templateSheetName = templateSheetName;
        this.headerName = headerName;
        this.mappingFieldName = mappingFieldName;
    }

    public Long getId() {
        return id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public String getTemplateSheetName() {
        return templateSheetName;
    }

    public String getHeaderName() {
        return headerName;
    }

    public String getMappingFieldName() {
        return mappingFieldName;
    }
}

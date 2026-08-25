package com.example.validation.application.port.out;

import com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping;

import java.util.List;
import java.util.Optional;

/**
 * 範本欄位對應儲存庫介面 (Outbound Port)
 * <p>
 * 定義 Application 層對於 {@link TemplateFieldMapping} 領域模型的持久化操作需求，
 * 供 Infrastructure 層的 Adapter 實作。藉此將資料存取技術細節與核心業務邏輯隔離。
 * </p>
 */
public interface TemplateFieldMappingRepositoryPort {
    
    /**
     * 儲存或更新範本欄位對應
     *
     * @param mapping 欲儲存的 {@link TemplateFieldMapping} 領域物件
     * @return 儲存成功後的 {@link TemplateFieldMapping} 領域物件
     */
    TemplateFieldMapping save(TemplateFieldMapping mapping);
    
    /**
     * 依據 ID 查詢範本欄位對應
     *
     * @param id 範本欄位對應 ID
     * @return 封裝查詢結果的 Optional 物件
     */
    Optional<TemplateFieldMapping> findById(Long id);
    
    /**
     * 依據 ID 刪除單一範本欄位對應
     *
     * @param id 欲刪除的範本欄位對應 ID
     */
    void deleteById(Long id);
    
    /**
     * 依據範本代碼查詢所有的範本欄位對應
     *
     * @param templateCode 範本代碼 (Template Code)
     * @return 符合該範本代碼的 {@link TemplateFieldMapping} 領域物件清單
     */
    List<TemplateFieldMapping> findByTemplateCode(String templateCode);
    
    /**
     * 依據範本代碼刪除其所有關聯的範本欄位對應資料
     *
     * @param templateCode 欲清除之範本代碼
     */
    void deleteByTemplateCode(String templateCode);
    
    /**
     * 依據範本代碼查詢該範本中所包含的不重複 Sheet 名稱清單
     *
     * @param templateCode 範本代碼
     * @return 不重複的 Sheet 頁籤名稱清單
     */
    List<String> findDistinctTemplateSheetNameByTemplateCode(String templateCode);
    
    /**
     * 依據範本代碼與特定的 Sheet 名稱，查詢所有的範本欄位對應
     *
     * @param templateCode      範本代碼
     * @param templateSheetName Excel Sheet 頁籤名稱
     * @return 符合條件的 {@link TemplateFieldMapping} 領域物件清單
     */
    List<TemplateFieldMapping> findByTemplateCodeAndTemplateSheetName(String templateCode, String templateSheetName);
}

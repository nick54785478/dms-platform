package com.dms.template.application.port.out;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.query.SearchTemplateQuery;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.aggregate.vo.TemplateId;

import java.util.Optional;

/**
 * 範本領域的出站埠 (Outbound Port / Repository Port).
 *
 * <p>
 * 在六角形架構 (Hexagonal Architecture) 中，此介面負責定義應用層對外部儲存設施的需求。
 * 其內部方法嚴格遵守依賴反轉原則 (DIP)，僅接收與回傳領域物件 (如 {@link Template})
 * 或應用層專屬的 DTO (如 {@link PagedResult}、{@link TemplateGottenResult})。
 * 絕對不包含任何與特定資料庫或框架 (如 JPA, Spring Data) 相關的技術細節。
 * </p>
 */
public interface TemplateRepositoryPort {
    /**
     * 儲存或更新範本領域物件 (Command Side).
     *
     * @param template 核心領域的範本聚合根實體
     */
    void save(Template template);

    /**
     * 根據識別碼尋找範本領域物件 (Command Side).
     *
     * @param id 範本的唯一識別碼 (Value Object)
     * @return 包含範本領域物件的 Optional，若找不到則回傳 empty
     */
    Optional<Template> findById(TemplateId id);
    
    /**
     * 搜尋範本列表 (Query Side).
     *
     * @param query 封裝了分頁與搜尋條件的查詢物件
     * @return 包含查詢結果 DTO 與分頁資訊的純資料載體 (PagedResult)
     */
    PagedResult<TemplateSearchedResult> searchTemplates(SearchTemplateQuery query);

    /**
     * 獲取單一範本的詳細資訊供檢視使用 (Query Side).
     *
     * @param id 範本的字串識別碼
     * @return 包含範本檢視資料 DTO 的 Optional，若找不到則回傳 empty
     */
    Optional<TemplateGottenResult> getTemplate(String id);
}

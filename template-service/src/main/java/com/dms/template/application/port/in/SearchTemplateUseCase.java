package com.dms.template.application.port.in;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.query.SearchTemplateQuery;

/**
 * 查詢範本清單 UseCase (Inbound Port)
 * <p>
 * 負責處理查詢與搜尋 {@link com.dms.template.domain.template.aggregate.root.Template} 領域模型的應用邏輯，支援分頁。
 * </p>
 */
public interface SearchTemplateUseCase {
    
    /**
     * 執行範本搜尋作業
     *
     * @param query 包含查詢條件與分頁資訊的 Query 物件
     * @return 封裝查詢結果與分頁資訊的 {@link PagedResult} DTO
     */
    PagedResult<TemplateSearchedResult> searchTemplates(SearchTemplateQuery query);
}

package com.dms.template.application.port.in;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateVersionGottenResult;
import com.dms.template.application.query.ListTemplateVersionsQuery;

/**
 * 查詢範本版本歷史紀錄 UseCase (Inbound Port)
 * <p>
 * 負責處理查詢指定範本代碼下所有歷史版本紀錄的應用邏輯，支援分頁。
 * </p>
 */
public interface ListTemplateVersionsUseCase {
    
    /**
     * 執行查詢範本版本歷史作業
     *
     * @param query 包含範本代碼與分頁資訊的 Query 物件
     * @return 封裝歷史版本清單與分頁資訊的 {@link PagedResult} DTO
     */
    PagedResult<TemplateVersionGottenResult> listTemplateVersions(ListTemplateVersionsQuery query);
}

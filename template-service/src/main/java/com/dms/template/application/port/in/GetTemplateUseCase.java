package com.dms.template.application.port.in;

import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.query.GetTemplateQuery;

/**
 * 取得單筆範本詳細資訊 UseCase (Inbound Port)
 * <p>
 * 負責處理依據指定條件查詢單一 {@link com.dms.template.domain.template.aggregate.root.Template} 的應用邏輯。
 * </p>
 */
public interface GetTemplateUseCase {
    
    /**
     * 執行取得範本作業
     *
     * @param query 包含查詢條件的 Query 物件
     * @return 包含範本詳細資訊的 {@link TemplateGottenResult} DTO
     */
    TemplateGottenResult getTemplate(GetTemplateQuery query);
}

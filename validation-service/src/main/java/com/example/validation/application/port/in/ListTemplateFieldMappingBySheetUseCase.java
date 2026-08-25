package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.application.shared.query.ListTemplateFieldMappingBySheetQuery;

import java.util.List;

/**
 * 依據 Sheet 名稱查詢範本欄位對應清單 UseCase (Inbound Port)
 * <p>
 * 負責處理特定 Sheet 下的 {@link com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping} 清單查詢。
 * </p>
 */
public interface ListTemplateFieldMappingBySheetUseCase {
    
    /**
     * 執行查詢
     *
     * @param query 包含 Sheet 查詢條件的 Query 物件
     * @return 查詢結果的 DTO 清單
     */
    List<TemplateFieldMappingSearchedResult> list(ListTemplateFieldMappingBySheetQuery query);
}

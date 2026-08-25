package com.example.validation.application.port.in;

import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.application.shared.query.ListTemplateFieldMappingQuery;

import java.util.List;

/**
 * 查詢範本欄位對應清單 UseCase (Inbound Port)
 * <p>
 * 負責處理查詢多筆 {@link com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping} 的應用邏輯。
 * </p>
 */
public interface ListTemplateFieldMappingUseCase {
    
    /**
     * 執行查詢
     *
     * @param query 包含查詢條件的 Query 物件
     * @return 查詢結果的 DTO 清單
     */
    List<TemplateFieldMappingSearchedResult> list(ListTemplateFieldMappingQuery query);
}

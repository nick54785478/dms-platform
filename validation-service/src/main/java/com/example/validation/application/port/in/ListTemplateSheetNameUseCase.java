package com.example.validation.application.port.in;

import com.example.validation.application.shared.query.ListTemplateSheetNameQuery;

import java.util.List;

/**
 * 查詢範本包含的所有 Sheet 名稱清單 UseCase (Inbound Port)
 * <p>
 * 負責處理依據範本代碼，列出該範本中不重複的所有 Sheet 名稱之查詢。
 * </p>
 */
public interface ListTemplateSheetNameUseCase {
    
    /**
     * 執行查詢
     *
     * @param query 包含範本代碼等查詢條件的 Query 物件
     * @return 不重複的 Sheet 名稱清單
     */
    List<String> list(ListTemplateSheetNameQuery query);
}

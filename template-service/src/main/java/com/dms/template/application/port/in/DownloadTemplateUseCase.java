package com.dms.template.application.port.in;

import com.dms.template.application.dto.DocumentGeneratedResult;
import com.dms.template.application.query.DownloadTemplateQuery;

/**
 * 下載範本 UseCase (Inbound Port)
 * <p>
 * 負責處理下載指定實體範本檔案的應用邏輯，將範本內容封裝回傳。
 * </p>
 */
public interface DownloadTemplateUseCase {
    
    /**
     * 執行下載範本作業
     *
     * @param query 包含欲下載的範本 ID 等條件的 Query 物件
     * @return 封裝了下載檔名與位元組內容的 {@link DocumentGeneratedResult} DTO
     */
    DocumentGeneratedResult downloadTemplate(DownloadTemplateQuery query);
}

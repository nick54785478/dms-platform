package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.CreateTemplateFieldMappingCommand;

/**
 * 建立範本欄位對應 UseCase (Inbound Port)
 * <p>
 * 負責處理建立新的 {@link com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping} 的應用邏輯。
 * 接收包含建立資訊的 Command，並回傳建立完成的實體 ID。
 * </p>
 */
public interface CreateTemplateFieldMappingUseCase {
    
    /**
     * 執行建立範本欄位對應
     *
     * @param command 包含建立所需資料的 Command 物件
     * @return 建立成功後產生的對應實體 ID
     */
    Long create(CreateTemplateFieldMappingCommand command);
}

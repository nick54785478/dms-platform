package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.UpdateTemplateFieldMappingCommand;

/**
 * 更新範本欄位對應 UseCase (Inbound Port)
 * <p>
 * 負責處理更新現有 {@link com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping} 的應用邏輯。
 * </p>
 */
public interface UpdateTemplateFieldMappingUseCase {
    
    /**
     * 執行更新範本欄位對應
     *
     * @param command 包含更新所需資料的 Command 物件
     */
    void update(UpdateTemplateFieldMappingCommand command);
}

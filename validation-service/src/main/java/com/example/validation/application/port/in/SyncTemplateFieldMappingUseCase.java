package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.SyncTemplateFieldMappingCommand;

/**
 * 同步範本欄位對應 UseCase (Inbound Port)
 * <p>
 * 負責處理與外部系統 (如 template-service) 的範本欄位對應資料進行同步的應用邏輯。
 * 處理非同步或排程驅動的批次更新作業。
 * </p>
 */
public interface SyncTemplateFieldMappingUseCase {
    
    /**
     * 執行同步作業
     *
     * @param command 包含同步任務所需資料的 Command 物件
     */
    void sync(SyncTemplateFieldMappingCommand command);
}

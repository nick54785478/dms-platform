package com.dms.template.application.port.in;

import com.dms.template.application.command.PublishTemplateCommand;

/**
 * 正式上架範本 UseCase (Inbound Port)
 * <p>
 * 負責處理將草稿狀態的範本發布上架的應用邏輯，並會觸發相對應的上架領域事件 (TemplatePublishedEvent)。
 * </p>
 */
public interface PublishTemplateUseCase {
    
    /**
     * 執行上架範本作業
     *
     * @param command 包含欲上架範本 ID 資訊的 Command 物件
     */
    void publishTemplate(PublishTemplateCommand command);
}

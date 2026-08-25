package com.dms.template.application.port.in;

import com.dms.template.application.command.SaveTemplateDraftCommand;

/**
 * 儲存範本草稿 UseCase (Inbound Port)
 * <p>
 * 負責處理將建立中的範本儲存為草稿狀態的應用邏輯。
 * </p>
 */
public interface SaveTemplateDraftUseCase {
    
    /**
     * 執行儲存範本草稿作業
     *
     * @param command 包含草稿內容與 metadata 的 Command 物件
     */
    void saveTemplateDraft(SaveTemplateDraftCommand command);
}

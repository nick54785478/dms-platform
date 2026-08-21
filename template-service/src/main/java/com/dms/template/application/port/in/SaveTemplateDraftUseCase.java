package com.dms.template.application.port.in;

import com.dms.template.application.command.SaveTemplateDraftCommand;

/**
 * 儲存範本草稿的業務案例介面 (Inbound Port)
 */
public interface SaveTemplateDraftUseCase {
    void saveTemplateDraft(SaveTemplateDraftCommand command);
}

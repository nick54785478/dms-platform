package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.SyncTemplateFieldMappingCommand;

/**
 * 同步範本欄位對應的 UseCase (Inbound Port)
 */
public interface SyncTemplateFieldMappingUseCase {
    void sync(SyncTemplateFieldMappingCommand command);
}

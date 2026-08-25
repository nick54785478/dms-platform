package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.UpdateTemplateFieldMappingCommand;

public interface UpdateTemplateFieldMappingUseCase {
    void update(UpdateTemplateFieldMappingCommand command);
}

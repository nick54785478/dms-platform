package com.example.validation.application.port.in;

import com.example.validation.application.shared.command.CreateTemplateFieldMappingCommand;

public interface CreateTemplateFieldMappingUseCase {
    Long create(CreateTemplateFieldMappingCommand command);
}

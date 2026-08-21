package com.dms.template.application.port.in;

import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.dto.TemplateGottenResult;

/**
 * 建立範本的業務案例介面 (Inbound Port)
 */
public interface CreateTemplateUseCase {
    TemplateGottenResult createTemplate(CreateTemplateCommand command);
}

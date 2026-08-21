package com.dms.template.application.port.in;

import com.dms.template.application.command.PublishTemplateCommand;

/**
 * 發佈範本使用案例 (Inbound Port)
 */
public interface PublishTemplateUseCase {
    void publishTemplate(PublishTemplateCommand command);
}

package com.dms.template.application.assembler;

import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.domain.template.aggregate.entity.TemplateVersion;
import com.dms.template.domain.template.aggregate.root.Template;
import org.springframework.stereotype.Component;

/**
 * 領域模型轉換為 DTO 的組裝器
 */
@Component
public class TemplateDtoAssembler {
    
    public TemplateGottenResult toResult(Template template) {
        if (template == null) {
            return null;
        }
        String draftJson = template.getLatestVersion()
                .map(TemplateVersion::getContentDefinition)
                .orElse(null);
                
        String latestVersion = template.getLatestVersion()
                .map(TemplateVersion::getVersion)
                .orElse(null);

        return new TemplateGottenResult(
                template.getId().value(),
                template.getTemplateType() != null ? template.getTemplateType().name() : null,
                template.getTemplateCode(),
                template.getName(),
                template.getDescription(),
                draftJson,
                latestVersion
        );
    }
}

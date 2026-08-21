package com.dms.template.application.assembler;

import com.dms.template.application.dto.TemplateGottenResult;
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
        String draftJson = null;
        if (template.getVersions() != null && !template.getVersions().isEmpty()) {
            draftJson = template.getVersions().get(0).getContentDefinition();
        }

        return new TemplateGottenResult(
                template.getId().getValue(),
                template.getTemplateType() != null ? template.getTemplateType().name() : null,
                template.getTemplateCode(),
                template.getName(),
                template.getDescription(),
                draftJson
        );
    }
}

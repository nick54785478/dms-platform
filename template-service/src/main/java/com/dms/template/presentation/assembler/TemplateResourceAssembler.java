package com.dms.template.presentation.assembler;

import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.presentation.resource.in.CreateTemplateResource;
import com.dms.template.presentation.resource.out.TemplateCreatedResource;
import org.springframework.stereotype.Component;

/**
 * 表現層與應用層之間的資料轉換防腐層
 */
@Component
public class TemplateResourceAssembler {

    public CreateTemplateCommand toCommand(CreateTemplateResource resource) {
        return new CreateTemplateCommand(
                resource.templateType(),
                resource.templateCode(),
                resource.name(),
                resource.description()
        );
    }

    public com.dms.template.application.command.SaveTemplateDraftCommand toCommand(String templateId, com.dms.template.presentation.resource.in.SaveTemplateDraftResource resource) {
        return new com.dms.template.application.command.SaveTemplateDraftCommand(
                templateId,
                resource.contentDefinition(),
                resource.variables()
        );
    }

    public TemplateCreatedResource toResource(TemplateGottenResult result) {
        if (result == null) {
            return null;
        }
        return new TemplateCreatedResource(
                result.id(),
                result.templateType(),
                result.templateCode(),
                result.name(),
                result.description()
        );
    }

    public com.dms.template.presentation.resource.out.TemplateSearchedResource toSearchedResource(com.dms.template.application.dto.TemplateSearchedResult result) {
        if (result == null) return null;
        return new com.dms.template.presentation.resource.out.TemplateSearchedResource(
                result.id(),
                result.templateType(),
                result.templateCode(),
                result.name(),
                result.description()
        );
    }

    public com.dms.template.presentation.resource.out.TemplateRetrievedResource toRetrievedResource(TemplateGottenResult result) {
        if (result == null) return null;
        return new com.dms.template.presentation.resource.out.TemplateRetrievedResource(
                result.id(),
                result.templateType(),
                result.templateCode(),
                result.name(),
                result.description(),
                result.draftJson()
        );
    }
}

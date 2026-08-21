package com.dms.template.presentation.assembler;

import com.dms.template.application.command.CreateTemplateCommand;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.dto.TemplateVersionGottenResult;
import com.dms.template.presentation.resource.in.CreateTemplateResource;
import com.dms.template.presentation.resource.out.TemplateCreatedResource;
import com.dms.template.presentation.resource.out.TemplateRetrievedResource;
import com.dms.template.presentation.resource.out.TemplateSearchedResource;
import com.dms.template.presentation.resource.out.TemplateVersionRetrievedResource;
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

    public TemplateSearchedResource toSearchedResource(TemplateSearchedResult result) {
        if (result == null) return null;
        return new com.dms.template.presentation.resource.out.TemplateSearchedResource(
                result.id(),
                result.templateType(),
                result.templateCode(),
                result.name(),
                result.description()
        );
    }

    public TemplateRetrievedResource toRetrievedResource(TemplateGottenResult result) {
        if (result == null) return null;
        return new com.dms.template.presentation.resource.out.TemplateRetrievedResource(
                result.id(),
                result.templateType(),
                result.templateCode(),
                result.name(),
                result.description(),
                result.draftJson(),
                result.latestVersion()
        );
    }

    public TemplateVersionRetrievedResource toVersionRetrievedResource(TemplateVersionGottenResult result) {
        return new com.dms.template.presentation.resource.out.TemplateVersionRetrievedResource(
                result.version(),
                result.status(),
                result.contentDefinition()
        );
    }
}

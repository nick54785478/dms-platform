package com.example.validation.presentation.assembler;

import com.example.validation.application.shared.command.CreateTemplateFieldMappingCommand;
import com.example.validation.application.shared.command.UpdateTemplateFieldMappingCommand;
import com.example.validation.application.shared.dto.TemplateFieldMappingGottenResult;
import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.presentation.resource.in.CreateTemplateFieldMappingResource;
import com.example.validation.presentation.resource.in.UpdateTemplateFieldMappingResource;
import com.example.validation.presentation.resource.out.TemplateFieldMappingRetrievedResource;
import org.springframework.stereotype.Component;

@Component
public class TemplateFieldMappingResourceAssembler {

    public CreateTemplateFieldMappingCommand toCommand(CreateTemplateFieldMappingResource resource) {
        return new CreateTemplateFieldMappingCommand(
                resource.getTemplateCode(),
                resource.getTemplateSheetName(),
                resource.getHeaderName(),
                resource.getMappingFieldName()
        );
    }

    public UpdateTemplateFieldMappingCommand toCommand(Long id, UpdateTemplateFieldMappingResource resource) {
        return new UpdateTemplateFieldMappingCommand(
                id,
                resource.getTemplateCode(),
                resource.getTemplateSheetName(),
                resource.getHeaderName(),
                resource.getMappingFieldName()
        );
    }

    public TemplateFieldMappingRetrievedResource toResource(TemplateFieldMappingGottenResult result) {
        TemplateFieldMappingRetrievedResource resource = new TemplateFieldMappingRetrievedResource();
        resource.setId(result.getId());
        resource.setTemplateCode(result.getTemplateCode());
        resource.setTemplateSheetName(result.getTemplateSheetName());
        resource.setHeaderName(result.getHeaderName());
        resource.setMappingFieldName(result.getMappingFieldName());
        return resource;
    }

    public TemplateFieldMappingRetrievedResource toResource(TemplateFieldMappingSearchedResult result) {
        TemplateFieldMappingRetrievedResource resource = new TemplateFieldMappingRetrievedResource();
        resource.setId(result.getId());
        resource.setTemplateCode(result.getTemplateCode());
        resource.setTemplateSheetName(result.getTemplateSheetName());
        resource.setHeaderName(result.getHeaderName());
        resource.setMappingFieldName(result.getMappingFieldName());
        return resource;
    }
}

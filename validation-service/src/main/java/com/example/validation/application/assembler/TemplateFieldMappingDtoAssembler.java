package com.example.validation.application.assembler;

import com.example.validation.application.shared.dto.TemplateFieldMappingGottenResult;
import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping;
import org.springframework.stereotype.Component;

@Component
public class TemplateFieldMappingDtoAssembler {

    public TemplateFieldMappingGottenResult toGottenResult(TemplateFieldMapping mapping) {
        return new TemplateFieldMappingGottenResult(
                mapping.getId(),
                mapping.getTemplateCode(),
                mapping.getTemplateSheetName(),
                mapping.getHeaderName(),
                mapping.getMappingFieldName()
        );
    }

    public TemplateFieldMappingSearchedResult toSearchedResult(TemplateFieldMapping mapping) {
        return new TemplateFieldMappingSearchedResult(
                mapping.getId(),
                mapping.getTemplateCode(),
                mapping.getTemplateSheetName(),
                mapping.getHeaderName(),
                mapping.getMappingFieldName()
        );
    }
}

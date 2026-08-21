package com.dms.template.application.service;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.port.in.DownloadTemplateUseCase;
import com.dms.template.application.port.in.GetTemplateUseCase;
import com.dms.template.application.port.in.ListTemplateVersionsUseCase;
import com.dms.template.application.port.in.SearchTemplateUseCase;
import com.dms.template.application.port.out.TemplateRepositoryPort;
import com.dms.template.application.query.DownloadTemplateQuery;
import com.dms.template.application.query.GetTemplateQuery;
import com.dms.template.application.query.SearchTemplateQuery;
import com.dms.template.application.port.out.DocumentGeneratorPort;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.aggregate.vo.TemplateId;
import com.dms.template.domain.template.exception.TemplateNotFoundException;
import com.dms.template.infrastructure.util.ExcelUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
class TemplateQueryService implements SearchTemplateUseCase, GetTemplateUseCase, DownloadTemplateUseCase, ListTemplateVersionsUseCase {

    private final TemplateRepositoryPort templateRepositoryPort;

    @Override
    public PagedResult<TemplateSearchedResult> searchTemplates(SearchTemplateQuery query) {
        return templateRepositoryPort.searchTemplates(query);
    }

    @Override
    public TemplateGottenResult getTemplate(GetTemplateQuery query) {
        return templateRepositoryPort.getTemplate(query.id())
                .orElseThrow(() -> new TemplateNotFoundException(query.id()));
    }

    private final List<DocumentGeneratorPort> documentGenerators;

    @Override
    @Transactional(readOnly = true)
    public com.dms.template.application.dto.DocumentGeneratedResult downloadTemplate(DownloadTemplateQuery query){
        Template template = templateRepositoryPort.findById(TemplateId.of(query.templateId()))
                .orElseThrow(() -> new TemplateNotFoundException(query.templateId()));

        for (DocumentGeneratorPort generator : documentGenerators) {
            if (generator.supports(template.getTemplateType())) {
                return generator.generate(template, query.data());
            }
        }
        
        throw new IllegalStateException("Unsupported template type: " + template.getTemplateType());
    }

    @Override
    public PagedResult<com.dms.template.application.dto.TemplateVersionGottenResult> listTemplateVersions(com.dms.template.application.query.ListTemplateVersionsQuery query) {
        return templateRepositoryPort.getTemplateVersions(query.templateId(), query.page(), query.size());
    }
}

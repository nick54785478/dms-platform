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

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadTemplate(DownloadTemplateQuery query){
        Template template = templateRepositoryPort.findById(TemplateId.of(query.templateId()))
                .orElseThrow(() -> new TemplateNotFoundException(query.templateId()));

        String content = getTemplateContent(template);
        String sheetName = template.getName() != null ? template.getName() : "Template";
        
        List<String> headers = new ArrayList<>();
        List<Object[]> dataset = new ArrayList<>();
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(content);
            JsonNode columnsNode = rootNode.path("columns");
            
            if (columnsNode.isArray() && !columnsNode.isEmpty()) {
                Object[] rowData = new Object[columnsNode.size()];
                for (int i = 0; i < columnsNode.size(); i++) {
                    JsonNode colNode = columnsNode.get(i);
                    headers.add(colNode.path("header").asText());
                    
                    String field = colNode.path("field").asText();
                    if (query.data() != null && query.data().containsKey(field)) {
                        rowData[i] = query.data().get(field);
                    } else {
                        rowData[i] = "";
                    }
                }
                dataset.add(rowData);
            } else {
                headers.add("No Columns Defined");
            }
        } catch (Exception e) {
            headers.add("Invalid Template JSON");
        }

        return ExcelUtil.exportDataAsByteArrayFromArrays(sheetName, headers, dataset);
    }

    private String getTemplateContent(Template template) {
        return template.getLatestVersion()
                .map(com.dms.template.domain.template.aggregate.entity.TemplateVersion::getContentDefinition)
                .orElse("{}");
    }

    @Override
    public PagedResult<com.dms.template.application.dto.TemplateVersionGottenResult> listTemplateVersions(com.dms.template.application.query.ListTemplateVersionsQuery query) {
        return templateRepositoryPort.getTemplateVersions(query.templateId(), query.page(), query.size());
    }
}

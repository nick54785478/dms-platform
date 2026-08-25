package com.example.validation.application.service;

import com.example.validation.application.assembler.TemplateFieldMappingDtoAssembler;
import com.example.validation.application.port.in.ListTemplateFieldMappingUseCase;
import com.example.validation.application.port.out.TemplateFieldMappingRepositoryPort;
import com.example.validation.application.shared.dto.TemplateFieldMappingSearchedResult;
import com.example.validation.application.shared.query.ListTemplateFieldMappingQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.example.validation.application.port.in.ListTemplateSheetNameUseCase;
import com.example.validation.application.port.in.ListTemplateFieldMappingBySheetUseCase;
import com.example.validation.application.shared.query.ListTemplateSheetNameQuery;
import com.example.validation.application.shared.query.ListTemplateFieldMappingBySheetQuery;

@Service
@Transactional(readOnly = true)
class TemplateFieldMappingQueryService implements ListTemplateFieldMappingUseCase, ListTemplateSheetNameUseCase, ListTemplateFieldMappingBySheetUseCase {

    private final TemplateFieldMappingRepositoryPort repositoryPort;
    private final TemplateFieldMappingDtoAssembler dtoAssembler;

    TemplateFieldMappingQueryService(TemplateFieldMappingRepositoryPort repositoryPort, TemplateFieldMappingDtoAssembler dtoAssembler) {
        this.repositoryPort = repositoryPort;
        this.dtoAssembler = dtoAssembler;
    }

    @Override
    public List<TemplateFieldMappingSearchedResult> list(ListTemplateFieldMappingQuery query) {
        return repositoryPort.findByTemplateCode(query.getTemplateCode()).stream()
                .map(dtoAssembler::toSearchedResult)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> list(ListTemplateSheetNameQuery query) {
        return repositoryPort.findDistinctTemplateSheetNameByTemplateCode(query.getTemplateCode());
    }

    @Override
    public List<TemplateFieldMappingSearchedResult> list(ListTemplateFieldMappingBySheetQuery query) {
        return repositoryPort.findByTemplateCodeAndTemplateSheetName(query.getTemplateCode(), query.getTemplateSheetName()).stream()
                .map(dtoAssembler::toSearchedResult)
                .collect(Collectors.toList());
    }
}

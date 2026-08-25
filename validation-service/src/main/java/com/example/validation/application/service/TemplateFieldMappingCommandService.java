package com.example.validation.application.service;

import com.example.validation.application.port.in.CreateTemplateFieldMappingUseCase;
import com.example.validation.application.port.in.DeleteTemplateFieldMappingUseCase;
import com.example.validation.application.port.in.UpdateTemplateFieldMappingUseCase;
import com.example.validation.application.port.out.TemplateFieldMappingRepositoryPort;
import com.example.validation.application.shared.command.CreateTemplateFieldMappingCommand;
import com.example.validation.application.shared.command.UpdateTemplateFieldMappingCommand;
import com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import com.example.validation.application.port.in.SyncTemplateFieldMappingUseCase;
import com.example.validation.application.shared.command.SyncTemplateFieldMappingCommand;

@Service
class TemplateFieldMappingCommandService implements 
        CreateTemplateFieldMappingUseCase, 
        UpdateTemplateFieldMappingUseCase, 
        DeleteTemplateFieldMappingUseCase,
        SyncTemplateFieldMappingUseCase {

    private final TemplateFieldMappingRepositoryPort repositoryPort;
    private final ObjectMapper objectMapper;

    TemplateFieldMappingCommandService(TemplateFieldMappingRepositoryPort repositoryPort, ObjectMapper objectMapper) {
        this.repositoryPort = repositoryPort;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Long create(CreateTemplateFieldMappingCommand command) {
        TemplateFieldMapping mapping = TemplateFieldMapping.create(
                command.getTemplateCode(),
                command.getTemplateSheetName(),
                command.getHeaderName(),
                command.getMappingFieldName()
        );
        TemplateFieldMapping savedMapping = repositoryPort.save(mapping);
        return savedMapping.getId();
    }

    @Override
    @Transactional
    public void update(UpdateTemplateFieldMappingCommand command) {
        TemplateFieldMapping mapping = repositoryPort.findById(command.getId())
                .orElseThrow(() -> new IllegalArgumentException("Mapping not found with id: " + command.getId()));

        mapping.update(
                command.getTemplateCode(),
                command.getTemplateSheetName(),
                command.getHeaderName(),
                command.getMappingFieldName()
        );
        repositoryPort.save(mapping);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public void sync(SyncTemplateFieldMappingCommand command) {
        if ("PDF".equalsIgnoreCase(command.getTemplateType())) {
            // PDF 可能不使用傳統的 Header/Field columns 結構，或是暫時不需要同步
            return;
        }

        // 1. 刪除舊的 Mapping
        repositoryPort.deleteByTemplateCode(command.getTemplateCode());

        // 2. 解析 JSON
        try {
            Map<String, Object> contentMap = objectMapper.readValue(command.getContentDefinition(), new TypeReference<Map<String, Object>>() {});
            if (contentMap != null) {
                if (contentMap.containsKey("sheets")) {
                    List<Map<String, Object>> sheets = (List<Map<String, Object>>) contentMap.get("sheets");
                    for (Map<String, Object> sheet : sheets) {
                        String sheetName = (String) sheet.get("sheetName");
                        List<Map<String, String>> columns = (List<Map<String, String>>) sheet.get("columns");
                        if (columns != null) {
                            for (Map<String, String> col : columns) {
                                String header = col.get("header");
                                String field = col.get("field");
                                if (header != null && !header.isBlank() && field != null && !field.isBlank()) {
                                    TemplateFieldMapping mapping = TemplateFieldMapping.create(
                                            command.getTemplateCode(),
                                            sheetName != null ? sheetName : "",
                                            header,
                                            field
                                    );
                                    repositoryPort.save(mapping);
                                }
                            }
                        }
                    }
                } else if (contentMap.containsKey("columns")) {
                    List<Map<String, String>> columns = (List<Map<String, String>>) contentMap.get("columns");
                    for (Map<String, String> col : columns) {
                        String sheetName = col.get("sheetName");
                        String header = col.get("header");
                        String field = col.get("field");
                        if (header != null && !header.isBlank() && field != null && !field.isBlank()) {
                            TemplateFieldMapping mapping = TemplateFieldMapping.create(
                                    command.getTemplateCode(),
                                    sheetName != null ? sheetName : "",
                                    header,
                                    field
                            );
                            repositoryPort.save(mapping);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse contentDefinition for syncing TemplateFieldMapping", e);
        }
    }
}

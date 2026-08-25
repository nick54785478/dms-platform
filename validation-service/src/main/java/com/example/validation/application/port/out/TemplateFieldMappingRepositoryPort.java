package com.example.validation.application.port.out;

import com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping;

import java.util.List;
import java.util.Optional;

public interface TemplateFieldMappingRepositoryPort {
    TemplateFieldMapping save(TemplateFieldMapping mapping);
    Optional<TemplateFieldMapping> findById(Long id);
    void deleteById(Long id);
    List<TemplateFieldMapping> findByTemplateCode(String templateCode);
    void deleteByTemplateCode(String templateCode);
    List<String> findDistinctTemplateSheetNameByTemplateCode(String templateCode);
    List<TemplateFieldMapping> findByTemplateCodeAndTemplateSheetName(String templateCode, String templateSheetName);
}

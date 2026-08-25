package com.example.validation.infrastructure.adapter;

import com.example.validation.application.port.out.TemplateFieldMappingRepositoryPort;
import com.example.validation.domain.mapping.aggregate.root.TemplateFieldMapping;
import com.example.validation.infrastructure.persistence.mapping.entity.TemplateFieldMappingJpaEntity;
import com.example.validation.infrastructure.persistence.mapping.repository.TemplateFieldMappingJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
class TemplateFieldMappingRepositoryAdapter implements TemplateFieldMappingRepositoryPort {

    private final TemplateFieldMappingJpaRepository jpaRepository;

    TemplateFieldMappingRepositoryAdapter(TemplateFieldMappingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TemplateFieldMapping save(TemplateFieldMapping mapping) {
        TemplateFieldMappingJpaEntity entity = toJpaEntity(mapping);
        TemplateFieldMappingJpaEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<TemplateFieldMapping> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<TemplateFieldMapping> findByTemplateCode(String templateCode) {
        return jpaRepository.findByTemplateCode(templateCode).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByTemplateCode(String templateCode) {
        jpaRepository.deleteByTemplateCode(templateCode);
    }

    @Override
    public List<String> findDistinctTemplateSheetNameByTemplateCode(String templateCode) {
        return jpaRepository.findDistinctTemplateSheetNameByTemplateCode(templateCode);
    }

    @Override
    public List<TemplateFieldMapping> findByTemplateCodeAndTemplateSheetName(String templateCode, String templateSheetName) {
        return jpaRepository.findByTemplateCodeAndTemplateSheetName(templateCode, templateSheetName).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private TemplateFieldMappingJpaEntity toJpaEntity(TemplateFieldMapping domain) {
        TemplateFieldMappingJpaEntity entity = new TemplateFieldMappingJpaEntity();
        entity.setId(domain.getId());
        entity.setTemplateCode(domain.getTemplateCode());
        entity.setTemplateSheetName(domain.getTemplateSheetName());
        entity.setHeaderName(domain.getHeaderName());
        entity.setMappingFieldName(domain.getMappingFieldName());
        return entity;
    }

    private TemplateFieldMapping toDomain(TemplateFieldMappingJpaEntity entity) {
        return TemplateFieldMapping.reconstitute(
                entity.getId(),
                entity.getTemplateCode(),
                entity.getTemplateSheetName(),
                entity.getHeaderName(),
                entity.getMappingFieldName()
        );
    }
}

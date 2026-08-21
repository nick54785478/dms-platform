package com.dms.template.infrastructure.adapter;

import com.dms.template.application.port.out.TemplateRepositoryPort;
import com.dms.template.domain.template.aggregate.entity.TemplateVersion;
import com.dms.template.domain.template.aggregate.root.Template;
import com.dms.template.domain.template.aggregate.vo.TemplateId;
import com.dms.template.domain.template.aggregate.vo.TemplateType;
import com.dms.template.infrastructure.persistence.template.entity.TemplateJpaEntity;
import com.dms.template.infrastructure.persistence.template.entity.TemplateVersionJpaEntity;
import com.dms.template.infrastructure.persistence.template.repository.TemplateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.dms.template.application.dto.PagedResult;
import com.dms.template.application.dto.TemplateGottenResult;
import com.dms.template.application.dto.TemplateSearchedResult;
import com.dms.template.application.query.SearchTemplateQuery;

/**
 * 範本儲存庫適配器 (Outbound Adapter)
 * 實作 Repository Port，使用 JPA 進行資料持久化
 * package-private 限制只能透過 Port 存取
 */
@Component
@RequiredArgsConstructor
class TemplateRepositoryAdapter implements TemplateRepositoryPort {

    private final TemplateJpaRepository jpaRepository;

    @Override
    public void save(Template template) {
        TemplateJpaEntity entity = jpaRepository.findById(template.getId().getValue())
                .orElseGet(TemplateJpaEntity::new);
                
        entity.setId(template.getId().getValue());
        entity.setTemplateType(template.getTemplateType() != null ? template.getTemplateType().name() : null);
        entity.setTemplateCode(template.getTemplateCode());
        entity.setName(template.getName());
        entity.setDescription(template.getDescription());
        
        // Map versions
        entity.getVersions().clear();
        if (template.getVersions() != null) {
            for (TemplateVersion v : template.getVersions()) {
                TemplateVersionJpaEntity versionEntity = new TemplateVersionJpaEntity();
                versionEntity.setTemplate(entity);
                versionEntity.setVersion(v.getVersion());
                versionEntity.setContentDefinition(v.getContentDefinition());
                versionEntity.setStatus(v.getStatus() != null ? v.getStatus().name() : null);
                // Variables mapped as JSON string (omitted/empty list handled simply)
                versionEntity.setVariablesJson("[]"); 
                entity.getVersions().add(versionEntity);
            }
        }
        
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Template> findById(TemplateId id) {
        return jpaRepository.findById(id.getValue()).map(entity -> {
            TemplateType type = entity.getTemplateType() != null ? TemplateType.valueOf(entity.getTemplateType()) : null;
            
            List<TemplateVersion> domainVersions = new ArrayList<>();
            if (entity.getVersions() != null) {
                for (com.dms.template.infrastructure.persistence.template.entity.TemplateVersionJpaEntity vEntity : entity.getVersions()) {
                    com.dms.template.domain.template.aggregate.vo.TemplateStatus status = vEntity.getStatus() != null 
                        ? com.dms.template.domain.template.aggregate.vo.TemplateStatus.valueOf(vEntity.getStatus()) 
                        : com.dms.template.domain.template.aggregate.vo.TemplateStatus.DRAFT;
                    
                    domainVersions.add(TemplateVersion.reconstitute(
                        vEntity.getVersion(), 
                        vEntity.getContentDefinition(), 
                        status, 
                        new ArrayList<>() // Empty variables for now
                    ));
                }
            }
            
            return Template.reconstitute(TemplateId.of(entity.getId()), type, entity.getTemplateCode(), entity.getName(), entity.getDescription(), domainVersions); 
        });
    }

    @Override
    public PagedResult<TemplateSearchedResult> searchTemplates(SearchTemplateQuery query) {
        TemplateJpaEntity probe = new TemplateJpaEntity();
        if (query.templateType() != null && !query.templateType().isBlank()) {
            probe.setTemplateType(query.templateType());
        }
        if (query.templateCode() != null && !query.templateCode().isBlank()) {
            probe.setTemplateCode(query.templateCode());
        }
        if (query.name() != null && !query.name().isBlank()) {
            probe.setName(query.name());
        }

        org.springframework.data.domain.ExampleMatcher matcher = org.springframework.data.domain.ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(org.springframework.data.domain.ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();

        org.springframework.data.domain.Example<TemplateJpaEntity> example = org.springframework.data.domain.Example.of(probe, matcher);
        org.springframework.data.domain.PageRequest pageRequest = org.springframework.data.domain.PageRequest.of(query.page(), query.size());

        org.springframework.data.domain.Page<TemplateJpaEntity> pageResult = jpaRepository.findAll(example, pageRequest);
        
        List<TemplateSearchedResult> content = pageResult.map(entity -> {
            return new TemplateSearchedResult(
                entity.getId(),
                entity.getTemplateType(),
                entity.getTemplateCode(),
                entity.getName(),
                entity.getDescription()
            );
        }).getContent();

        return new PagedResult<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    @Override
    public Optional<TemplateGottenResult> getTemplate(String id) {
        return jpaRepository.findById(id).map(entity -> {
            String draftJson = null;
            if (entity.getVersions() != null && !entity.getVersions().isEmpty()) {
                draftJson = entity.getVersions().getFirst().getContentDefinition();
            }
            return new TemplateGottenResult(
                    entity.getId(),
                    entity.getTemplateType(),
                    entity.getTemplateCode(),
                    entity.getName(),
                    entity.getDescription(),
                    draftJson
            );
        });
    }
}

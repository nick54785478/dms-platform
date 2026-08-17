package com.example.dms.infrastructure.adapter;

import com.example.dms.application.port.out.DocumentRepositoryPort;
import com.example.dms.application.shared.dto.PageGottenResult;
import com.example.dms.domain.document.aggregate.entity.DocumentVersion;
import com.example.dms.domain.document.aggregate.root.Document;
import com.example.dms.domain.document.aggregate.vo.DocumentId;
import com.example.dms.domain.document.aggregate.vo.DocumentVersionId;
import com.example.dms.infrastructure.persistence.document.entity.DocumentJpaEntity;
import com.example.dms.infrastructure.persistence.document.entity.DocumentVersionJpaEntity;
import com.example.dms.infrastructure.persistence.document.repository.DocumentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import com.example.dms.domain.document.aggregate.vo.DocumentStatus;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 文件的資料庫存取適配器 (Outbound Adapter)
 * 宣告為 package-private 以隱藏實作細節，僅透過 DocumentRepositoryPort 介面開放
 */
@Component
@RequiredArgsConstructor
class DocumentRepositoryAdapter implements DocumentRepositoryPort {

    private final DocumentJpaRepository repository;

    @Override
    public Document save(Document document) {
        DocumentJpaEntity entity = toEntity(document);
        DocumentJpaEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Document> findById(DocumentId id) {
        return repository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public PageGottenResult<Document> searchDocuments(String title, String status, int page, int size) {
        Specification<DocumentJpaEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.isNotBlank(title)) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(status)) {
                try {
                    predicates.add(cb.equal(root.get("status"), DocumentStatus.valueOf(status)));
                } catch (IllegalArgumentException e) {
                    // Ignore invalid status
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<DocumentJpaEntity> entityPage = repository.findAll(spec, PageRequest.of(page, size));
        List<Document> documents = entityPage.getContent().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());

        return PageGottenResult.<Document>builder()
                .content(documents)
                .pageNumber(entityPage.getNumber())
                .pageSize(entityPage.getSize())
                .totalElements(entityPage.getTotalElements())
                .totalPages(entityPage.getTotalPages())
                .build();
    }

    private DocumentJpaEntity toEntity(Document document) {
        DocumentJpaEntity entity = new DocumentJpaEntity();
        entity.setId(document.getId().getValue());
        entity.setTitle(document.getTitle());
        entity.setDescription(document.getDescription());
        entity.setFileId(document.getFileId());
        entity.setStatus(document.getStatus());
        entity.setCreatedAt(document.getCreatedAt());
        entity.setUpdatedAt(document.getUpdatedAt());
        entity.setMajorVersion(document.getMajorVersion());
        entity.setMinorVersion(document.getMinorVersion());

        List<DocumentVersionJpaEntity> historyEntities = document.getHistory().stream().map(version -> {
            DocumentVersionJpaEntity versionEntity = new DocumentVersionJpaEntity();
            versionEntity.setId(version.getId().getValue());
            versionEntity.setMajorVersion(version.getMajorVersion());
            versionEntity.setMinorVersion(version.getMinorVersion());
            versionEntity.setTitle(version.getTitle());
            versionEntity.setDescription(version.getDescription());
            versionEntity.setFileId(version.getFileId());
            versionEntity.setCreatedAt(version.getCreatedAt());
            return versionEntity;
        }).collect(Collectors.toList());
        
        entity.setHistory(historyEntities);
        return entity;
    }

    private Document toDomain(DocumentJpaEntity entity) {
        List<DocumentVersion> history = entity.getHistory().stream().map(versionEntity -> new DocumentVersion(
                DocumentVersionId.of(versionEntity.getId()),
                versionEntity.getMajorVersion(),
                versionEntity.getMinorVersion(),
                versionEntity.getTitle(),
                versionEntity.getDescription(),
                versionEntity.getFileId(),
                versionEntity.getCreatedAt()
        )).collect(Collectors.toList());

        return Document.reconstitute(
                DocumentId.of(entity.getId()),
                entity.getTitle(),
                entity.getDescription(),
                entity.getFileId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getMajorVersion(),
                entity.getMinorVersion(),
                history
        );
    }
}

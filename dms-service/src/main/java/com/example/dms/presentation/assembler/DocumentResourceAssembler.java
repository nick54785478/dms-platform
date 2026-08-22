package com.example.dms.presentation.assembler;

import com.example.dms.application.shared.command.CreateDocumentCommand;
import com.example.dms.application.shared.command.UpdateDocumentCommand;
import com.example.dms.application.shared.dto.DocumentGottenResult;
import com.example.dms.presentation.resource.in.CreateDocumentResource;
import com.example.dms.presentation.resource.in.UpdateDocumentResource;
import com.example.dms.presentation.resource.in.UploadDocumentVersionResource;
import com.example.dms.presentation.resource.out.DocumentCreatedResource;
import com.example.dms.presentation.resource.out.DocumentRetrievedResource;
import com.example.dms.presentation.resource.out.DocumentSearchedResource;
import com.example.dms.presentation.resource.out.DocumentVersionResource;
import com.example.dms.presentation.resource.out.PageRetrievedResource;
import org.springframework.stereotype.Component;

/**
 * 負責 Presentation Layer 與 Application Layer 之間的資料轉換與防腐
 */
@Component
public class DocumentResourceAssembler {

    public CreateDocumentCommand toCommand(CreateDocumentResource resource) {
        if (resource == null) return null;
        return new CreateDocumentCommand(
                resource.getTitle(),
                resource.getDescription(),
                resource.getFileId()
        );
    }

    public DocumentCreatedResource toResource(DocumentGottenResult result) {
        if (result == null) return null;
        return new DocumentCreatedResource(
                result.id(),
                result.title(),
                result.description(),
                result.fileId(),
                result.status(),
                result.createdAt()
        );
    }

    public DocumentRetrievedResource toRetrievedResource(DocumentGottenResult result) {
        if (result == null) return null;
        
        java.util.List<DocumentVersionResource> history = result.history() != null
                ? result.history().stream().map(h -> DocumentVersionResource.builder()
                        .versionId(h.versionId())
                        .majorVersion(h.majorVersion())
                        .minorVersion(h.minorVersion())
                        .semanticVersion(h.getSemanticVersion())
                        .title(h.title())
                        .description(h.description())
                        .fileId(h.fileId())
                        .createdAt(h.createdAt())
                        .build()).collect(java.util.stream.Collectors.toList())
                : java.util.Collections.emptyList();

        return DocumentRetrievedResource.builder()
                .id(result.id())
                .title(result.title())
                .description(result.description())
                .fileId(result.fileId())
                .status(result.status())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .semanticVersion(result.semanticVersion())
                .history(history)
                .build();
    }

    public UpdateDocumentCommand toCommand(String id, UpdateDocumentResource resource) {
        if (resource == null) return null;
        return new UpdateDocumentCommand(
                id,
                resource.getTitle(),
                resource.getDescription()
        );
    }

    public com.example.dms.application.shared.command.UploadDocumentVersionCommand toCommand(String id, UploadDocumentVersionResource resource) {
        if (resource == null) return null;
        return new com.example.dms.application.shared.command.UploadDocumentVersionCommand(
                id,
                resource.getFileId(),
                resource.isMajorVersion()
        );
    }

    public PageRetrievedResource<DocumentSearchedResource> toResource(com.example.dms.application.shared.dto.PageGottenResult<com.example.dms.application.shared.dto.DocumentSearchedResult> result) {
        if (result == null) return null;
        
        java.util.List<DocumentSearchedResource> content = result.content().stream()
                .map(r -> DocumentSearchedResource.builder()
                        .id(r.id())
                        .title(r.title())
                        .description(r.description())
                        .fileId(r.fileId())
                        .status(r.status() != null ? r.status().name() : null)
                        .createdAt(r.createdAt())
                        .updatedAt(r.updatedAt())
                        .semanticVersion(r.semanticVersion())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return PageRetrievedResource.<DocumentSearchedResource>builder()
                .content(content)
                .pageNumber(result.pageNumber())
                .pageSize(result.pageSize())
                .totalElements(result.totalElements())
                .totalPages(result.totalPages())
                .build();
    }
}

package com.example.dms.application.service;

import com.example.dms.application.shared.command.CreateDocumentCommand;
import com.example.dms.application.port.in.CreateDocumentUseCase;
import com.example.dms.application.port.in.DeleteDocumentCommand;
import com.example.dms.application.port.in.DeleteDocumentUseCase;
import com.example.dms.application.port.in.SearchDocumentQuery;
import com.example.dms.application.port.in.SearchDocumentUseCase;
import com.example.dms.application.port.in.UpdateDocumentCommand;
import com.example.dms.application.port.in.UpdateDocumentUseCase;
import com.example.dms.application.port.out.DocumentRepositoryPort;
import com.example.dms.application.port.out.EventOutboxPort;
import com.example.dms.application.shared.dto.DocumentGottenResult;
import com.example.dms.application.shared.dto.DocumentSearchedResult;
import com.example.dms.application.shared.dto.PageGottenResult;
import com.example.dms.application.shared.event.AttachmentDeletedEvent;
import com.example.dms.application.shared.event.FileBoundEvent;
import com.example.dms.domain.document.aggregate.root.Document;
import com.example.dms.domain.document.aggregate.vo.DocumentId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 處理 Document 相關業務的應用服務 (Application Service)
 * 依照規範宣告為 package-private，外部須透過 Inbound Port 介面呼叫
 */
@Service
@RequiredArgsConstructor
class DocumentApplicationService implements CreateDocumentUseCase, DeleteDocumentUseCase, UpdateDocumentUseCase, SearchDocumentUseCase, com.example.dms.application.port.in.UploadDocumentVersionUseCase, com.example.dms.application.port.in.GetDocumentUseCase {

    private final DocumentRepositoryPort documentRepositoryPort;
    private final EventOutboxPort eventOutboxPort;

    @Override
    @Transactional
    public DocumentGottenResult createDocument(CreateDocumentCommand command) {
        Document document = Document.create(
                command.title(),
                command.description(),
                command.fileId()
        );

        Document savedDocument = documentRepositoryPort.save(document);

        if (StringUtils.isNotBlank(savedDocument.getFileId())) {
            FileBoundEvent event = new FileBoundEvent(
                    savedDocument.getFileId(),
                    null,
                    "DOCUMENT",
                    savedDocument.getId().getValue()
            );
            
            eventOutboxPort.saveEvent(event);
        }

        return DocumentGottenResult.fromDomain(savedDocument);
    }

    @Override
    @Transactional
    public void deleteDocument(DeleteDocumentCommand command) {
        Document document = documentRepositoryPort.findById(new DocumentId(command.getDocumentId()))
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        document.delete();
        documentRepositoryPort.save(document);

        if (StringUtils.isNotBlank(document.getFileId())) {
            AttachmentDeletedEvent event = new AttachmentDeletedEvent(
                    document.getFileId(),
                    null,
                    "DOCUMENT",
                    document.getId().getValue()
            );
                    
            eventOutboxPort.saveEvent(event);
        }
    }

    @Override
    @Transactional
    public DocumentGottenResult updateDocument(UpdateDocumentCommand command) {
        Document document = documentRepositoryPort.findById(new DocumentId(command.getDocumentId()))
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        document.update(command.getTitle(), command.getDescription());
        Document savedDocument = documentRepositoryPort.save(document);
        
        return DocumentGottenResult.fromDomain(savedDocument);
    }

    @Override
    @Transactional
    public DocumentGottenResult uploadDocumentVersion(com.example.dms.application.shared.command.UploadDocumentVersionCommand command) {
        Document document = documentRepositoryPort.findById(new DocumentId(command.documentId()))
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        document.uploadNewVersion(command.fileId(), command.isMajorVersion());
        Document savedDocument = documentRepositoryPort.save(document);
        
        if (StringUtils.isNotBlank(savedDocument.getFileId())) {
            FileBoundEvent event = new FileBoundEvent(
                    savedDocument.getFileId(),
                    null,
                    "DOCUMENT",
                    savedDocument.getId().getValue()
            );
            
            eventOutboxPort.saveEvent(event);
        }
        
        return DocumentGottenResult.fromDomain(savedDocument);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentGottenResult getDocument(com.example.dms.application.shared.command.GetDocumentQuery query) {
        Document document = documentRepositoryPort.findById(new DocumentId(query.documentId()))
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return DocumentGottenResult.fromDomain(document);
    }

    @Override
    @Transactional(readOnly = true)
    public PageGottenResult<DocumentSearchedResult> searchDocuments(SearchDocumentQuery query) {
        PageGottenResult<Document> domainPage = documentRepositoryPort.searchDocuments(query.getTitle(), query.getStatus(), query.getPage(), query.getSize());
        
        List<DocumentSearchedResult> content = domainPage.content().stream()
                .map(DocumentSearchedResult::fromDomain)
                .collect(Collectors.toList());
                
        return PageGottenResult.<DocumentSearchedResult>builder()
                .content(content)
                .pageNumber(domainPage.pageNumber())
                .pageSize(domainPage.pageSize())
                .totalElements(domainPage.totalElements())
                .totalPages(domainPage.totalPages())
                .build();
    }
}

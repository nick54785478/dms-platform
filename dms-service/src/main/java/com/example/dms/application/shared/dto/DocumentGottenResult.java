package com.example.dms.application.shared.dto;

import com.example.dms.domain.document.aggregate.root.Document;
import java.time.LocalDateTime;

/**
 * 文件的純資料載體 (Application Layer)
 * 供 Presentation Layer 使用
 */
public record DocumentGottenResult(
        String id,
        String title,
        String description,
        String fileId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String semanticVersion,
        java.util.List<DocumentVersionGottenResult> history
) {
    public static DocumentGottenResult fromDomain(Document document) {
        if (document == null) return null;
        
        java.util.List<DocumentVersionGottenResult> history = document.getHistory() != null 
                ? document.getHistory().stream().map(DocumentVersionGottenResult::fromDomain).collect(java.util.stream.Collectors.toList())
                : java.util.Collections.emptyList();

        return new DocumentGottenResult(
                document.getId().getValue(),
                document.getTitle(),
                document.getDescription(),
                document.getFileId(),
                document.getStatus().name(),
                document.getCreatedAt(),
                document.getUpdatedAt(),
                document.getSemanticVersion(),
                history
        );
    }
}

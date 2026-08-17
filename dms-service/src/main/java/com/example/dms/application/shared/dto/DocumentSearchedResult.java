package com.example.dms.application.shared.dto;

import com.example.dms.domain.document.aggregate.root.Document;
import com.example.dms.domain.document.aggregate.vo.DocumentStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record DocumentSearchedResult(
        String id,
        String title,
        String description,
        String fileId,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String semanticVersion
) {
    public static DocumentSearchedResult fromDomain(Document document) {
        return DocumentSearchedResult.builder()
                .id(document.getId().getValue())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileId(document.getFileId())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .semanticVersion(document.getSemanticVersion())
                .build();
    }
}

package com.example.demo.presentation.dto;

import com.example.demo.domain.file.aggregate.root.FileMetadata;

import java.time.LocalDateTime;
import java.util.Map;

public record FileResponse(
    String id,
    String originalFileName,
    String mimeType,
    Long size,
    String checksum,
    String type,
    Map<String, String> tags,
    String status,
    LocalDateTime createdAt
) {
    public static FileResponse fromDomain(FileMetadata metadata) {
        if (metadata == null) return null;
        return new FileResponse(
            metadata.getId(),
            metadata.getOriginalFileName(),
            metadata.getMimeType(),
            metadata.getSize(),
            metadata.getChecksum(),
            metadata.getType(),
            metadata.getTags(),
            metadata.getStatus() != null ? metadata.getStatus().name() : null,
            metadata.getCreatedAt()
        );
    }
}

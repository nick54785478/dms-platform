package com.example.dms.application.shared.dto;

import com.example.dms.domain.document.aggregate.entity.DocumentVersion;

import java.time.LocalDateTime;

public record DocumentVersionGottenResult(
        String versionId,
        int majorVersion,
        int minorVersion,
        String title,
        String description,
        String fileId,
        LocalDateTime createdAt
) {
    public static DocumentVersionGottenResult fromDomain(DocumentVersion version) {
        if (version == null) return null;
        return new DocumentVersionGottenResult(
                version.getId().getValue(),
                version.getMajorVersion(),
                version.getMinorVersion(),
                version.getTitle(),
                version.getDescription(),
                version.getFileId(),
                version.getCreatedAt()
        );
    }
    
    public String getSemanticVersion() {
        return "V" + majorVersion + "." + minorVersion;
    }
}

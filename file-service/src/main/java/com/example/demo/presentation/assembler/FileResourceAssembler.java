package com.example.demo.presentation.assembler;

import com.example.demo.domain.file.aggregate.root.FileMetadata;
import com.example.demo.presentation.resource.out.FileUploadedResource;
import org.springframework.stereotype.Component;

@Component
public class FileResourceAssembler {

    public FileUploadedResource toFileUploadedResource(FileMetadata metadata) {
        if (metadata == null) return null;
        return new FileUploadedResource(
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

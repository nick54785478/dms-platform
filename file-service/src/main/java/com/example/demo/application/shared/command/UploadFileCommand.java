package com.example.demo.application.shared.command;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public record UploadFileCommand(
    String tenantId,
    String type,
    String originalFileName,
    String mimeType,
    Long size,
    String checksum,
    Map<String, String> tags,
    MultipartFile file
) {}

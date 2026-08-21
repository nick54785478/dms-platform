package com.example.demo.presentation.resource.out;

import java.time.LocalDateTime;
import java.util.Map;

public record FileUploadedResource(
    String id,
    String originalFileName,
    String mimeType,
    Long size,
    String checksum,
    String type,
    Map<String, String> tags,
    String status,
    LocalDateTime createdAt
) {}

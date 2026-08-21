package com.example.demo.presentation.resource.in;

import java.util.Map;

public record GeneratePresignedUploadResource(
    String type,
    String originalFileName,
    String mimeType,
    Long size,
    String checksum,
    Map<String, String> tags,
    int expiryMinutes
) {}

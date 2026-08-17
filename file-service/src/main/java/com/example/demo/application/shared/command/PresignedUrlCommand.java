package com.example.demo.application.shared.command;

import java.util.Map;

public record PresignedUrlCommand(
    String tenantId,
    String type,
    String originalFileName,
    String mimeType,
    Long size,
    String checksum,
    Map<String, String> tags,
    int expiryMinutes
) {}

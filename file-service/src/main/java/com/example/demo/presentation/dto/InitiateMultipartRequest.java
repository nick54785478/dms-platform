package com.example.demo.presentation.dto;

import java.util.Map;

public record InitiateMultipartRequest(
    String type,
    String originalFileName,
    String mimeType,
    Long size,
    String checksum,
    Map<String, String> tags
) {}

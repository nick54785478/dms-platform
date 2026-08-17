package com.example.demo.presentation.dto.event;

public record FileBoundEventMessage(
    String fileId,
    String tenantId
) {}

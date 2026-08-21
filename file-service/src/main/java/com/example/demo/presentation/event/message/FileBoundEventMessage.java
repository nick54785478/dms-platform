package com.example.demo.presentation.event.message;

public record FileBoundEventMessage(
    String fileId,
    String tenantId
) {}

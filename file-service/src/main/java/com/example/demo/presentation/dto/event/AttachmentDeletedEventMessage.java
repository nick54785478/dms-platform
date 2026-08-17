package com.example.demo.presentation.dto.event;

public record AttachmentDeletedEventMessage(
    String fileId,
    String tenantId
) {}

package com.example.demo.presentation.event.message;

public record AttachmentDeletedEventMessage(
    String fileId,
    String tenantId
) {}

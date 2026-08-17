package com.example.dms.application.shared.event;

import com.example.dms.domain.shared.event.DomainEvent;
import java.time.LocalDateTime;
import java.util.UUID;

public record AttachmentDeletedEvent(
    UUID eventId,
    String fileId,
    String tenantId,
    String aggregateType,
    String aggregateId,
    LocalDateTime occurredAt
) implements DomainEvent {

    public AttachmentDeletedEvent(String fileId, String tenantId, String aggregateType, String aggregateId) {
        this(UUID.randomUUID(), fileId, tenantId, aggregateType, aggregateId, LocalDateTime.now());
    }
}

package com.example.dms.presentation.scheduler;

import com.example.dms.application.port.out.MessagePublisherPort;
import com.example.dms.application.shared.event.AttachmentDeletedEvent;
import com.example.dms.application.shared.event.FileBoundEvent;
import com.example.dms.infrastructure.persistence.outbox.entity.DomainEventOutboxJpaEntity;
import com.example.dms.infrastructure.persistence.outbox.repository.DomainEventOutboxJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class OutboxMessageRelay {

    private final DomainEventOutboxJpaRepository outboxRepository;
    private final MessagePublisherPort messagePublisherPort;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.relay.delay:5000}")
    @Transactional
    public void relayEvents() {
        List<DomainEventOutboxJpaEntity> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(DomainEventOutboxJpaEntity.OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending events in Outbox. Processing...", pendingEvents.size());

        for (DomainEventOutboxJpaEntity entity : pendingEvents) {
            try {
                if (FileBoundEvent.class.getName().equals(entity.getEventType())) {
                    FileBoundEvent event = objectMapper.readValue(entity.getPayload(), FileBoundEvent.class);
                    messagePublisherPort.publishFileBoundEvent(event);
                } else if (AttachmentDeletedEvent.class.getName().equals(entity.getEventType())) {
                    AttachmentDeletedEvent event = objectMapper.readValue(entity.getPayload(), AttachmentDeletedEvent.class);
                    messagePublisherPort.publishAttachmentDeletedEvent(event);
                } else {
                    log.warn("Unknown event type found in Outbox: {}. Skipping.", entity.getEventType());
                }

                entity.setStatus(DomainEventOutboxJpaEntity.OutboxStatus.PUBLISHED);
                outboxRepository.save(entity);
                
                log.debug("Successfully relayed event: {}", entity.getId());
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize event payload for event ID: {}", entity.getId(), e);
                // Optionally mark as FAILED instead of keeping it PENDING depending on strategy
            } catch (Exception e) {
                log.error("Failed to publish event ID: {}. Will retry later.", entity.getId(), e);
                // We do not change status, allowing retry on next run.
            }
        }
    }
}

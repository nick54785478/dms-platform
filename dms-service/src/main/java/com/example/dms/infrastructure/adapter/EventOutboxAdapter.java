package com.example.dms.infrastructure.adapter;

import com.example.dms.application.port.out.EventOutboxPort;
import com.example.dms.domain.shared.event.DomainEvent;
import com.example.dms.infrastructure.persistence.outbox.entity.DomainEventOutboxJpaEntity;
import com.example.dms.infrastructure.persistence.outbox.repository.DomainEventOutboxJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class EventOutboxAdapter implements EventOutboxPort {

    private final DomainEventOutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void saveEvent(DomainEvent event) {
        String aggregateId = event.aggregateId();
        String eventType = event.getClass().getName();
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            
            DomainEventOutboxJpaEntity entity = DomainEventOutboxJpaEntity.builder()
                    .aggregateType(event.aggregateType())
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payloadJson)
                    .status(DomainEventOutboxJpaEntity.OutboxStatus.PENDING)
                    .build();
                    
            outboxRepository.save(entity);
            log.debug("Successfully saved event to outbox. AggregateId: {}, EventType: {}", aggregateId, eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event payload to JSON. AggregateId: {}, EventType: {}", aggregateId, eventType, e);
            throw new RuntimeException("Failed to serialize event payload", e);
        }
    }
}

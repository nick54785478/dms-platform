package com.example.dms.infrastructure.adapter;

import com.example.dms.application.port.out.MessagePublisherPort;
import com.example.dms.application.shared.event.AttachmentDeletedEvent;
import com.example.dms.application.shared.event.FileBoundEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class KafkaMessagePublisherAdapter implements MessagePublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String FILE_BOUND_TOPIC = "file.bound.events";
    private static final String ATTACHMENT_DELETED_TOPIC = "attachment.deleted.events";

    @Override
    public void publishFileBoundEvent(FileBoundEvent event) {
        log.info("Publishing FileBoundEvent for fileId: {}", event.fileId());
        kafkaTemplate.send(FILE_BOUND_TOPIC, event.fileId(), event);
    }

    @Override
    public void publishAttachmentDeletedEvent(AttachmentDeletedEvent event) {
        log.info("Publishing AttachmentDeletedEvent for fileId: {}", event.fileId());
        kafkaTemplate.send(ATTACHMENT_DELETED_TOPIC, event.fileId(), event);
    }
}

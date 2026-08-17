package com.example.demo.presentation.event;

import com.example.demo.application.port.in.DeleteFileUseCase;
import com.example.demo.application.shared.event.AttachmentDeletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentDeletedEventConsumer {

    private final DeleteFileUseCase deleteFileUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "attachment.deleted.events", groupId = "file-service-group")
    public void consume(String message) {
        log.info("Received AttachmentDeletedEvent message: {}", message);
        try {
            AttachmentDeletedEvent event = objectMapper.readValue(message, AttachmentDeletedEvent.class);
            deleteFileUseCase.deleteFile(event.getFileId());
        } catch (Exception e) {
            log.error("Failed to process AttachmentDeletedEvent: {}", message, e);
        }
    }
}

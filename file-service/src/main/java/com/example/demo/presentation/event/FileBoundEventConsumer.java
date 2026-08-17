package com.example.demo.presentation.event;

import com.example.demo.application.port.in.BindFileUseCase;
import com.example.demo.application.shared.event.FileBoundEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileBoundEventConsumer {

    private final BindFileUseCase bindFileUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "file.bound.events", groupId = "file-service-group")
    public void consume(String message) {
        log.info("Received FileBoundEvent message: {}", message);
        try {
            FileBoundEvent event = objectMapper.readValue(message, FileBoundEvent.class);
            bindFileUseCase.bindFile(event.getFileId());
        } catch (Exception e) {
            log.error("Failed to process FileBoundEvent: {}", message, e);
        }
    }
}

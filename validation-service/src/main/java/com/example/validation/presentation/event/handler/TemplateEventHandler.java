package com.example.validation.presentation.event.handler;

import com.example.validation.application.port.in.SyncTemplateFieldMappingUseCase;
import com.example.validation.application.shared.command.SyncTemplateFieldMappingCommand;
import com.example.validation.presentation.event.message.TemplatePublishedEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 範本事件監聽器 (Presentation Layer - Inbound Adapter)。
 * <p>
 * 負責監聽來自 Template Service 的 Kafka 領域事件，
 * 並將其轉換為應用層的 Command，交由應用層 UseCase 處理。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class  TemplateEventHandler {

    private final SyncTemplateFieldMappingUseCase syncUseCase;
    private final ObjectMapper objectMapper;

    /**
     * 處理範本發佈事件。
     * <p>
     * 負責將 JSON 格式的訊息反序列化為 {@link TemplatePublishedEventMessage}，
     * 並轉換為 {@link SyncTemplateFieldMappingCommand}，進行範本欄位對應的同步。
     * </p>
     *
     * @param message 來自 Kafka 的 JSON 格式事件訊息
     */
    @KafkaListener(topics = "template-events", groupId = "validation-service-group")
    public void onTemplatePublishedEvent(String message) {
        log.info("Received TemplatePublishedEvent from Kafka: {}", message);
        try {
            // 1. 解析訊息為事件 DTO
            TemplatePublishedEventMessage event = objectMapper.readValue(message, TemplatePublishedEventMessage.class);
            
            // 2. 將事件 DTO 轉換為應用層 Command
            SyncTemplateFieldMappingCommand command = new SyncTemplateFieldMappingCommand(
                    event.getTemplateCode(),
                    event.getTemplateType(),
                    event.getContentDefinition()
            );
            
            // 3. 調用 Inbound Port (UseCase) 執行同步
            syncUseCase.sync(command);
            
            log.info("Successfully synced TemplateFieldMapping for template: {}", event.getTemplateCode());
        } catch (Exception e) {
            log.error("Failed to process TemplatePublishedEvent: {}", message, e);
        }
    }
}

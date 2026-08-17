package com.example.demo.presentation.event;

import com.example.demo.application.port.in.HandleFileEventUseCase;
import com.example.demo.application.shared.command.FileBoundCommand;
import com.example.demo.application.shared.command.FileDeletedCommand;
import com.example.demo.presentation.dto.event.AttachmentDeletedEventMessage;
import com.example.demo.presentation.dto.event.FileBoundEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 檔案事件監聽器 (Presentation Layer - Inbound Adapter)。
 * <p>
 * 負責監聽來自外部 Message Broker (Kafka) 的領域事件，並將其轉換為應用層的 Command，
 * 然後委派給對應的 {@link HandleFileEventUseCase} 進行業務處理。
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FileEventListener {

    private final HandleFileEventUseCase handleFileEventUseCase;
    private final ObjectMapper objectMapper;

    /**
     * 處理檔案綁定事件。
     * <p>
     * 當接收到 {@code topic.file.bound} 主題的訊息時觸發。
     * 負責將 JSON 格式的訊息反序列化為 {@link FileBoundEventMessage}，
     * 並轉換為 {@link FileBoundCommand}，交由應用層 UseCase 處理。
     * </p>
     *
     * @param message 來自 Kafka 的 JSON 格式事件訊息
     */
    @KafkaListener(topics = "topic.file.bound", groupId = "${spring.kafka.consumer.group-id:file-service-group}")
    public void onFileBoundEvent(String message) {
        log.info("Received FileBoundEvent: {}", message);
        try {
            // 1. 解析訊息為事件 DTO
            FileBoundEventMessage event = objectMapper.readValue(message, FileBoundEventMessage.class);
            // 2. 將事件 DTO 轉換為應用層 Command
            FileBoundCommand command = new FileBoundCommand(event.fileId(), event.tenantId());
            // 3. 調用 Inbound Port (UseCase) 執行業務邏輯
            handleFileEventUseCase.handleFileBoundEvent(command);
        } catch (Exception e) {
            log.error("Error processing FileBoundEvent: {}", e.getMessage(), e);
            // In a real system, you might want to send this to a Dead Letter Queue (DLQ)
        }
    }

    /**
     * 處理附件刪除事件。
     * <p>
     * 當接收到 {@code topic.attachment.deleted} 主題的訊息時觸發。
     * 負責將 JSON 格式的訊息反序列化為 {@link AttachmentDeletedEventMessage}，
     * 並轉換為 {@link FileDeletedCommand}，交由應用層 UseCase 處理以進行檔案刪除。
     * </p>
     *
     * @param message 來自 Kafka 的 JSON 格式事件訊息
     */
    @KafkaListener(topics = "topic.attachment.deleted", groupId = "${spring.kafka.consumer.group-id:file-service-group}")
    public void onAttachmentDeletedEvent(String message) {
        log.info("Received AttachmentDeletedEvent: {}", message);
        try {
            // 1. 解析訊息為事件 DTO
            AttachmentDeletedEventMessage event = objectMapper.readValue(message, AttachmentDeletedEventMessage.class);
            // 2. 將事件 DTO 轉換為應用層 Command
            FileDeletedCommand command = new FileDeletedCommand(event.fileId(), event.tenantId());
            // 3. 調用 Inbound Port (UseCase) 執行業務邏輯
            handleFileEventUseCase.handleFileDeletedEvent(command);
        } catch (Exception e) {
            log.error("Error processing AttachmentDeletedEvent: {}", e.getMessage(), e);
            // DLQ handling
        }
    }
}

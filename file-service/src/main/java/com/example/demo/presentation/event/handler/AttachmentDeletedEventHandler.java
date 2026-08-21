package com.example.demo.presentation.event.handler;

import com.example.demo.application.port.in.DeleteFileUseCase;
import com.example.demo.application.shared.event.AttachmentDeletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 附件刪除事件處理器 (Presentation Layer - Inbound Adapter)。
 * <p>
 * 負責監聽來自外部 Message Broker (Kafka) 的 {@code attachment.deleted.events} 事件，
 * 將 JSON 訊息反序列化為領域事件，並委派給應用層的 {@link DeleteFileUseCase} 執行檔案的實體刪除與狀態更新邏輯。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentDeletedEventHandler {

    private final DeleteFileUseCase deleteFileUseCase;
    private final ObjectMapper objectMapper;

    /**
     * 接收並處理附件刪除事件。
     *
     * @param message 來自 Kafka 的 JSON 格式事件訊息字串
     */
    @KafkaListener(topics = "attachment.deleted.events", groupId = "file-service-group")
    public void consume(String message) {
        log.info("Received AttachmentDeletedEvent message: {}", message);
        try {
            // 1. 將接收到的 JSON 字串反序列化為 AttachmentDeletedEvent 物件
            AttachmentDeletedEvent event = objectMapper.readValue(message, AttachmentDeletedEvent.class);
            // 2. 呼叫應用層 UseCase，傳入 FileId 以進行檔案刪除操作
            deleteFileUseCase.deleteFile(event.getFileId());
        } catch (Exception e) {
            log.error("Failed to process AttachmentDeletedEvent: {}", message, e);
        }
    }
}

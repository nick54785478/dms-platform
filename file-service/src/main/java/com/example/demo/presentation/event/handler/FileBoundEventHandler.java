package com.example.demo.presentation.event.handler;

import com.example.demo.application.port.in.BindFileUseCase;
import com.example.demo.application.shared.event.FileBoundEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 檔案綁定事件處理器 (Presentation Layer - Inbound Adapter)。
 * <p>
 * 負責監聽來自外部 Message Broker (Kafka) 的 {@code file.bound.events} 事件，
 * 將 JSON 訊息反序列化為領域事件，並委派給應用層的 {@link BindFileUseCase} 執行檔案的正式綁定邏輯 (例如標記為永久保存)。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileBoundEventHandler {

    private final BindFileUseCase bindFileUseCase;
    private final ObjectMapper objectMapper;

    /**
     * 接收並處理檔案綁定事件。
     *
     * @param message 來自 Kafka 的 JSON 格式事件訊息字串
     */
    @KafkaListener(topics = "file.bound.events", groupId = "file-service-group")
    public void consume(String message) {
        log.info("Received FileBoundEvent message: {}", message);
        try {
            // 1. 將接收到的 JSON 字串反序列化為 FileBoundEvent 物件
            FileBoundEvent event = objectMapper.readValue(message, FileBoundEvent.class);
            // 2. 呼叫應用層 UseCase，傳入 FileId 以進行檔案綁定操作
            bindFileUseCase.bindFile(event.getFileId());
        } catch (Exception e) {
            log.error("Failed to process FileBoundEvent: {}", message, e);
        }
    }
}

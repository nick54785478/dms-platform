package com.dms.template.application.port.out;

import com.dms.template.domain.template.event.TemplatePublishedEvent;

/**
 * 範本訊息發布器介面 (Outbound Port)
 * <p>
 * 定義 Application 層對於發布領域事件的通訊操作需求，
 * 供 Infrastructure 層的 Adapter (如 Kafka/RabbitMQ Adapter) 進行實作。
 * 藉由依賴反轉，確保領域與應用層不相依於底層的 Message Broker 技術。
 * </p>
 */
public interface TemplateMessagePublisherPort {
    
    /**
     * 發布範本已正式上架 (Published) 的領域事件
     *
     * @param event 封裝了上架範本資訊的 {@link TemplatePublishedEvent} 事件物件
     */
    void publishTemplatePublishedEvent(TemplatePublishedEvent event);
}

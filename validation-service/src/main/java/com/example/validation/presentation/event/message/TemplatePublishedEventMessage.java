package com.example.validation.presentation.event.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 來自外部 Kafka (Template Service) 的領域事件訊息 (Inbound Message DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePublishedEventMessage {
    private String templateCode;
    private String templateType;
    private String contentDefinition;
}

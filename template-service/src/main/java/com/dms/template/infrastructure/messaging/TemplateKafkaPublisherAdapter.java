package com.dms.template.infrastructure.messaging;

import com.dms.template.application.port.out.TemplateMessagePublisherPort;
import com.dms.template.domain.template.event.TemplatePublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 範本訊息發佈器實作 (Outbound Adapter)
 */
@Slf4j
@Component
@RequiredArgsConstructor
class TemplateKafkaPublisherAdapter implements TemplateMessagePublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "template-events";

    @Override
    public void publishTemplatePublishedEvent(TemplatePublishedEvent event) {
        log.info("Publishing TemplatePublishedEvent to Kafka. TemplateCode: {}", event.getTemplateCode());
        kafkaTemplate.send(TOPIC, event.getTemplateCode(), event);
    }
}

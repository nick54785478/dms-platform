package com.dms.template.application.port.out;

import com.dms.template.domain.template.event.TemplatePublishedEvent;

/**
 * 範本訊息發佈器 (Outbound Port)
 */
public interface TemplateMessagePublisherPort {
    void publishTemplatePublishedEvent(TemplatePublishedEvent event);
}

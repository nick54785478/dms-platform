package com.example.dms.application.port.out;

import com.example.dms.domain.shared.event.DomainEvent;

public interface EventOutboxPort {
    /**
     * 儲存領域事件到 Outbox 發件箱中
     * 
     * @param event 實作了 DomainEvent 的領域事件
     */
    void saveEvent(DomainEvent event);
}

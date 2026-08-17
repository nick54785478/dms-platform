package com.example.dms.application.port.out;

import com.example.dms.application.shared.event.AttachmentDeletedEvent;
import com.example.dms.application.shared.event.FileBoundEvent;

public interface MessagePublisherPort {
    void publishFileBoundEvent(FileBoundEvent event);
    void publishAttachmentDeletedEvent(AttachmentDeletedEvent event);
}

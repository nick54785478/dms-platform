package com.example.dms.application.port.in;

import com.example.dms.application.shared.command.CreateDocumentCommand;
import com.example.dms.application.shared.dto.DocumentGottenResult;

/**
 * 建立文件的使用案例 (Inbound Port)
 */
public interface CreateDocumentUseCase {
    DocumentGottenResult createDocument(CreateDocumentCommand command);
}

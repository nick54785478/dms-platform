package com.example.dms.application.port.in;

import com.example.dms.application.shared.command.DeleteDocumentCommand;

public interface DeleteDocumentUseCase {
    void deleteDocument(DeleteDocumentCommand command);
}

package com.example.dms.application.port.in;

import com.example.dms.application.shared.command.UpdateDocumentCommand;

import com.example.dms.application.shared.dto.DocumentGottenResult;

public interface UpdateDocumentUseCase {
    DocumentGottenResult updateDocument(UpdateDocumentCommand command);
}

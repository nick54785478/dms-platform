package com.example.dms.application.port.in;

import com.example.dms.application.shared.query.GetDocumentQuery;
import com.example.dms.application.shared.dto.DocumentGottenResult;

public interface GetDocumentUseCase {
    DocumentGottenResult getDocument(GetDocumentQuery query);
}

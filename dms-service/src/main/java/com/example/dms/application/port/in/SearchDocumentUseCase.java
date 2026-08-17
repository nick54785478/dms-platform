package com.example.dms.application.port.in;

import com.example.dms.application.shared.dto.DocumentSearchedResult;
import com.example.dms.application.shared.dto.PageGottenResult;

public interface SearchDocumentUseCase {
    PageGottenResult<DocumentSearchedResult> searchDocuments(SearchDocumentQuery query);
}

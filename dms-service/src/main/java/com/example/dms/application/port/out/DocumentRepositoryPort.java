package com.example.dms.application.port.out;

import com.example.dms.application.shared.dto.PageGottenResult;
import com.example.dms.domain.document.aggregate.root.Document;
import com.example.dms.domain.document.aggregate.vo.DocumentId;

import java.util.Optional;

/**
 * 文件資料庫存取介面 (Outbound Port)
 */
public interface DocumentRepositoryPort {
    Document save(Document document);
    Optional<Document> findById(DocumentId id);
    PageGottenResult<Document> searchDocuments(String title, String status, int page, int size);
}

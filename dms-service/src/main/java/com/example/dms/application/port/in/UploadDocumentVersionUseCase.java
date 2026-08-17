package com.example.dms.application.port.in;

import com.example.dms.application.shared.command.UploadDocumentVersionCommand;
import com.example.dms.application.shared.dto.DocumentGottenResult;

/**
 * 上傳新版本文件的 Inbound Port (Use Case)
 */
public interface UploadDocumentVersionUseCase {
    DocumentGottenResult uploadDocumentVersion(UploadDocumentVersionCommand command);
}

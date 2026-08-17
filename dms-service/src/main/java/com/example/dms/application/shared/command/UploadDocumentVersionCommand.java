package com.example.dms.application.shared.command;

/**
 * 上傳新版本文件的 Command (Application Layer)
 */
public record UploadDocumentVersionCommand(
        String documentId,
        String fileId,
        boolean isMajorVersion
) {
}

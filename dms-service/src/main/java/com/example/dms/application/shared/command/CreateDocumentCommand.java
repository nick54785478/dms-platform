package com.example.dms.application.shared.command;

/**
 * 建立文件的 Command (Application Layer)
 */
public record CreateDocumentCommand(
        String title,
        String description,
        String fileId
) {
}

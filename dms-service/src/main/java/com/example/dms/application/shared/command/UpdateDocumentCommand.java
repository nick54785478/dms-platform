package com.example.dms.application.shared.command;

public record UpdateDocumentCommand(
        String documentId,
        String title,
        String description
) {
}

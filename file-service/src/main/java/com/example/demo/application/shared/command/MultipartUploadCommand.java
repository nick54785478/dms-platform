package com.example.demo.application.shared.command;

import java.util.Map;

public class MultipartUploadCommand {

    public record InitiateCommand(
        String tenantId,
        String type,
        String originalFileName,
        String mimeType,
        Long size,
        String checksum,
        Map<String, String> tags
    ) {}

    public record CompleteCommand(
        String fileId,
        String uploadId,
        Map<Integer, String> partETags
    ) {}

    public record AbortCommand(
        String fileId,
        String uploadId
    ) {}
}

package com.example.demo.application.shared.command;

public record FileBoundCommand(
    String fileId,
    String tenantId
) {}

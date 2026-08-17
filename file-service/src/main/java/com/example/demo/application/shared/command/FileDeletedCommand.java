package com.example.demo.application.shared.command;

public record FileDeletedCommand(
    String fileId,
    String tenantId
) {}

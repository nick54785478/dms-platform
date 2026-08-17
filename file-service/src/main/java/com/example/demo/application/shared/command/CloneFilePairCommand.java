package com.example.demo.application.shared.command;

public record CloneFilePairCommand(
    String sourceKey,
    String targetKey
) {}

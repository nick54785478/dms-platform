package com.dms.template.application.dto;

public record DocumentGeneratedResult(
    byte[] content,
    String fileName,
    String contentType
) {}

package com.dms.template.application.dto;

public record TemplateGottenResult(
    String id,
    String templateType,
    String templateCode,
    String name,
    String description,
    String draftJson,
    String latestVersion
) {}

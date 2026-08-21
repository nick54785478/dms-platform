package com.dms.template.application.dto;

public record TemplateSearchedResult(
    String id,
    String templateType,
    String templateCode,
    String name,
    String description
) {}

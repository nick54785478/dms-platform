package com.dms.template.presentation.resource.out;

public record TemplateSearchedResource(
    String id,
    String templateType,
    String templateCode,
    String name,
    String description
) {}

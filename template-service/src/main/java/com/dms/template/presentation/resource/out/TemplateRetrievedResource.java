package com.dms.template.presentation.resource.out;

public record TemplateRetrievedResource(
    String id,
    String templateType,
    String templateCode,
    String name,
    String description,
    String draftJson
) {}

package com.dms.template.application.query;

public record ListTemplateVersionsQuery(
    String templateId,
    int page,
    int size
) {}
